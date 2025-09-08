package org.example.test_tierbot.Service.Manager;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.weaver.ast.Call;
import org.example.test_tierbot.Entity.Action;
import org.example.test_tierbot.Entity.Role;
import org.example.test_tierbot.Entity.User;
import org.example.test_tierbot.Entity.UserDetails;
import org.example.test_tierbot.Entity.timetable.Timetable;
import org.example.test_tierbot.Entity.timetable.Weekday;
import org.example.test_tierbot.Repository.TimetableRepository;
import org.example.test_tierbot.Repository.UserDetailsRep;
import org.example.test_tierbot.Repository.UserRepository;
import org.example.test_tierbot.Service.Factory.AnswerMethodFactory;
import org.example.test_tierbot.Service.Factory.KeyboardFactory;
import org.example.test_tierbot.TelegramBot.TelegramBot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.example.test_tierbot.Service.data.CallBackData.*;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class TimetableManager extends AbstractManager {

    final AnswerMethodFactory methodFactory;
    final KeyboardFactory keyboardFactory;
    final UserRepository userRepository;
    final TimetableRepository timetableRepository;
    final UserDetailsRep userDetailsRep;

    @Autowired
    public TimetableManager(AnswerMethodFactory methodFactory, KeyboardFactory keyboardFactory, UserRepository userRepository, TimetableRepository timetableRepository, UserDetailsRep userDetailsRep) {
        this.methodFactory = methodFactory;
        this.keyboardFactory = keyboardFactory;
        this.userRepository = userRepository;
        this.timetableRepository = timetableRepository;
        this.userDetailsRep = userDetailsRep;
    }

    @Override
    public BotApiMethod<?> answerCommand(Message message, TelegramBot bot) {
        return mainMenu(message);
    }

    @Transactional
    @Override
    public BotApiMethod<?> answerCallbackQuery(CallbackQuery callbackQuery, TelegramBot bot) {
        String callbackData = callbackQuery.getData();
        String[] splitData = callbackData.split("_");
        if (splitData.length > 1 && ADD.equals(splitData[1])) {
            if (splitData.length == 2 || splitData.length == 3) {
                return add(callbackQuery, splitData);
            }
            switch (splitData[2]){
                case WEEKDAY -> {
                    return addWeekDay(callbackQuery, splitData);
                }
                case HOUR -> {
                    return addHour(callbackQuery, splitData);
                }
                case MINUTE -> {
                    return addMinute(callbackQuery, splitData);
                }
                case USER -> {
                    return addUser(callbackQuery, splitData);
                }
                case TITTLE ->{
                    return editTittle(callbackQuery, splitData);
                }
                case DESCRIPTION ->{
                    return editDescription(callbackQuery, splitData);
                }
        }
        }
        switch (callbackData){
            case TIMETABLE -> {
                return mainMenu(callbackQuery);
            }
            case TIMETABLE_SHOW -> {
                return show(callbackQuery);
            }
            case TIMETABLE_REMOVE -> {
                return remove(callbackQuery);
            }
            case TIMETABLE_1,TIMETABLE_2,TIMETABLE_3,TIMETABLE_4,TIMETABLE_5,TIMETABLE_6,TIMETABLE_7 ->{
                return showDay(callbackQuery);
            }
        }
        if (splitData.length > 2 && REMOVE.equals(splitData[1])){
            switch (splitData[2]){
                case WEEKDAY -> {
                    return removeFromWeekday(callbackQuery, splitData[3]);
                }
                case POS -> {
                    return askConfirmation(callbackQuery, splitData);
                }
                case CONFIRM -> {
                    return removeLesson(callbackQuery, splitData[3], bot);
                }
            }
        }  if (FINISH.equals(splitData[1])){
            return finish(callbackQuery, splitData, bot);
        }
        return null;
    }

    @Override
    public BotApiMethod<?> answerMessage(Message message, TelegramBot bot) {
        var user = userRepository.findUserByChatId(message.getChatId());
        try {
            bot.execute(methodFactory.getDeleteMessage(message.getChatId(),
                    message.getMessageId() - 1));
            bot.execute(methodFactory.getSendMessage(
                    message.getChatId(),
                    "Значение успешно установлено",
                    null
            ));
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
        switch (user.getAction()){
            case SENDING_TITTLE -> {
                return setTittle(message, user);
            }
            case SENDING_DESCRIPTION -> {
                return setDescription(message, user);
            }
        }

        return null;
    }

    private BotApiMethod<?> mainMenu(Message message) {
        var user = userRepository.findUserByChatId(message.getChatId());
        if (user.getRole() == Role.STUDENT) {
            return methodFactory.getSendMessage(
                    message.getChatId(),
                    """
                            📆 Здесь вы можете посмотреть ваше расписание
                            """,
                    keyboardFactory.inlineKeyboardMarkup(
                            List.of("Показать расписание"),
                            List.of(1),
                            List.of(TIMETABLE_SHOW)
                    )
            );
        } else {
            return methodFactory.getSendMessage(
                    message.getChatId(),
                    """
                            📆 Здесь вы можете управлять вашим расписанием
                            """,
                    keyboardFactory.inlineKeyboardMarkup(
                            List.of("Показать занятия",
                                    "Добавить заниятие", "Удалить занятие"),
                            List.of(1, 2),
                            List.of(TIMETABLE_SHOW, TIMETABLE_ADD, TIMETABLE_REMOVE)
                    )
            );
        }
    }

    private BotApiMethod<?> mainMenu(CallbackQuery callbackQuery) {
        var user = userRepository.findUserByChatId(callbackQuery.getMessage().getChatId());
        if (user.getRole() == Role.STUDENT) {
            return methodFactory.getEditMessageText(
                    callbackQuery,
                    """
                            📆 Здесь вы можете посмотреть ваше расписание
                            """,
                    keyboardFactory.inlineKeyboardMarkup(
                            List.of("Показать расписание"),
                            List.of(1),
                            List.of(TIMETABLE_SHOW)
                    )
            );
        } else {
            return methodFactory.getEditMessageText(
                    callbackQuery,
                    """
                            📆 Здесь вы можете управлять вашим расписанием
                            """,
                    keyboardFactory.inlineKeyboardMarkup(
                            List.of("Показать занятия",
                                    "Добавить заниятие", "Удалить занятие"),
                            List.of(1, 2),
                            List.of(TIMETABLE_SHOW, TIMETABLE_ADD, TIMETABLE_REMOVE)
                    )
            );
        }
    }
    private BotApiMethod<?> show(CallbackQuery callbackQuery) {
        return methodFactory.getEditMessageText(
                callbackQuery,
                """
                        📆 Выберете день недели
                        """,
                keyboardFactory.inlineKeyboardMarkup(
                        List.of("Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота", "Воскресенье",
                                "Назад"),
                        List.of(7, 1),
                        List.of(TIMETABLE_1, TIMETABLE_2, TIMETABLE_3, TIMETABLE_4, TIMETABLE_5, TIMETABLE_6, TIMETABLE_7
                                ,TIMETABLE)
                )
        );
    }
    private BotApiMethod<?> showDay(CallbackQuery callbackQuery) {
        var user = userRepository.findUserByChatId(callbackQuery.getMessage().getChatId());
        Weekday weekday = null;
        switch (callbackQuery.getData().split("_")[1]){
            case "1" -> {
                weekday = Weekday.MONDAY;
            }
            case "2" -> {
                weekday = Weekday.TUESDAY;
            }
            case "3" -> {
                weekday = Weekday.WEDNESDAY;
            }
            case "4" -> {
                weekday = Weekday.THURSDAY;
            }
            case "5" -> {
                weekday = Weekday.FRIDAY;
            }
            case "6" -> {
                weekday = Weekday.SATURDAY;
            }
            case "7" -> {
                weekday = Weekday.SUNDAY;
            }

        }
        StringBuilder text = new StringBuilder();
        List<Timetable> timetableList = timetableRepository.findAllByUsersContainingAndWeekday(user, weekday);
        if (timetableList == null || timetableList.isEmpty()){
            text.append("У вас нет занятии на этот день");
        }
        else {
            text.append("Ваши занятия на сегодня :'\n'");
            for (Timetable timetable : timetableList){
                text.append("▪\uFE0F ")
                        .append(timetable.getHour())
                        .append(" : ")
                        .append(timetable.getMinute())
                        .append(" - ")
                        .append(timetable.getTittle());
            }
        }
        return methodFactory.getEditMessageText(callbackQuery,
                text.toString(),
                keyboardFactory.inlineKeyboardMarkup(
                        List.of("Назад"),
                        List.of(1),
                        List.of(TIMETABLE_SHOW)
                ));
    }

    private BotApiMethod<?> add(CallbackQuery callbackQuery, String[] splitData) {
        String id = "";
        if (splitData.length == 2){
            Timetable timetable = Timetable.builder()
                    .users(List.of(userRepository.findUserByChatId(callbackQuery.getMessage().getChatId())))
                    .build();
            timetable.setInCreation(true);
            id = timetableRepository.save(timetable).getId().toString();
        }
        else {
            id = splitData[2];
        }
        List<String> weekdays = new ArrayList<>();
        for (int i = 1; i <= 7; i++ ){
            weekdays.add(TIMETABLE_ADD_WEEKDAY_ + i + "_" + id);
        }
        weekdays.add(TIMETABLE);
        return methodFactory.getEditMessageText(
                callbackQuery,
                """
                        ✏️ Выберете день, в который хотите добавить занятие:
                        """,
                keyboardFactory.inlineKeyboardMarkup(
                        List.of("Понедельник","Вторник","Среда","Четверг","Пятница",
                                "СУббота","Воскресенье","Назад"),
                        List.of(7, 1),
                        weekdays
                )
        );
    }
    private BotApiMethod<?> addWeekDay(CallbackQuery callbackQuery, String[] splitData){
        UUID id = UUID.fromString(splitData[4]);
        var timetable = timetableRepository.findTimetableById(id);
        switch (splitData[3]){
            case "1" -> timetable.setWeekday(Weekday.MONDAY);
            case "2" -> timetable.setWeekday(Weekday.TUESDAY);
            case "3" -> timetable.setWeekday(Weekday.WEDNESDAY);
            case "4" -> timetable.setWeekday(Weekday.THURSDAY);
            case "5" -> timetable.setWeekday(Weekday.FRIDAY);
            case "6" -> timetable.setWeekday(Weekday.SATURDAY);
            case "7" -> timetable.setWeekday(Weekday.SUNDAY);
        }
        List<String> dataButton = new ArrayList<>();
        List<String> text = new ArrayList<>();
        for (int i = 0; i < 24; i++){
            text.add(String.valueOf(i));
            dataButton.add(TIMETABLE_ADD_HOUR_ + i + "_" + splitData[4]);
        }
        text.add("Назад");
        dataButton.add(TIMETABLE_ADD + "_" + splitData[4]);
        timetableRepository.save(timetable);
        return methodFactory.getEditMessageText(
                callbackQuery,
                "Выберите час",
                keyboardFactory.inlineKeyboardMarkup(
                        text,
                        List.of(6, 6, 6, 6, 1),
                        dataButton
                )
        );

    }
    private BotApiMethod<?> addHour(CallbackQuery callbackQuery, String[] splitdata){
        UUID id = UUID.fromString(splitdata[4]);
        var timetable = timetableRepository.findTimetableById(id);
        timetable.setHour(Short.valueOf(splitdata[3]));
        List<String> dataButton = new ArrayList<>();
        List<String> text = new ArrayList<>();
        for (int i = 0; i <= 59; i++){
            text.add(String.valueOf(i));
            dataButton.add(TIMETABLE_ADD_MINUTE_ + i + "_" + id); // splitdata[4] == id?
        }
        text.add("Назад");
        switch (timetable.getWeekday()) {
            case MONDAY -> dataButton.add(TIMETABLE_ADD_WEEKDAY_ + 1 + "_" + id);
            case TUESDAY -> dataButton.add(TIMETABLE_ADD_WEEKDAY_ + 2 + "_" + id);
            case WEDNESDAY -> dataButton.add(TIMETABLE_ADD_WEEKDAY_ + 3 + "_" + id);
            case THURSDAY -> dataButton.add(TIMETABLE_ADD_WEEKDAY_ + 4 + "_" + id);
            case FRIDAY -> dataButton.add(TIMETABLE_ADD_WEEKDAY_ + 5 + "_" + id);
            case SATURDAY -> dataButton.add(TIMETABLE_ADD_WEEKDAY_ + 6 + "_" + id);
            case SUNDAY -> dataButton.add(TIMETABLE_ADD_WEEKDAY_ + 7 + "_" + id);
        }
//        dataButton.add(TIMETABLE_ADD + "_" + splitdata[4]);
        timetableRepository.save(timetable);
        return methodFactory.getEditMessageText(
                callbackQuery,
                "Выберите минуту",
                keyboardFactory.inlineKeyboardMarkup(
                        text,
                        List.of(6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 1),
                        dataButton
                )
        );
    }
    private BotApiMethod<?>  addMinute(CallbackQuery callbackQuery, String[] splitData){
        System.out.println(callbackQuery.getData());
        UUID id = UUID.fromString(splitData[4]);
        var timetable = timetableRepository.findTimetableById(id);
        timetable.setMinute(Short.valueOf(splitData[3]));
        List<String> dataButton = new ArrayList<>();
        List<String> text = new ArrayList<>();
        List<Integer> cfg = new ArrayList<>();
        int index = 0;
        var person = userRepository.findUserByChatId(callbackQuery.getMessage().getChatId());
        for (User user : person.getUsers()){
            text.add(user.getUserDetails().getFirstName());
            dataButton.add(TIMETABLE_ADD_USER_ + user.getChatId() + "_" + id);
            if (index == 5){
                cfg.add(index);
                index = 0;
            }
            else {
                index += 1;
            }
        }
        if (index != 0) {
            cfg.add(index);
        }
        cfg.add(1);
        dataButton.add(TIMETABLE_ADD_HOUR_ + timetable.getHour() + "_" + id);
        text.add("Назад");
        timetableRepository.save(timetable);
        String messageText = "Выберите ученика";
        if (cfg.size() == 1){
            messageText = "У вас еще ни одного ученика";
        }

        return methodFactory.getEditMessageText(
                callbackQuery,
                messageText,
                keyboardFactory.inlineKeyboardMarkup(
                        text,
                        cfg,
                        dataButton
                )
        );
    }

    private BotApiMethod<?> addUser(CallbackQuery callbackQuery, String[] splitData){
        String id = splitData[4];
        var timetable = timetableRepository.findTimetableById(UUID.fromString(id));
        var user = userRepository.findUserByChatId(Long.valueOf(splitData[3]));
        timetable.addUser(user);
        timetable.setTittle(user.getUserDetails().getFirstName());
        timetableRepository.save(timetable);
        return methodFactory.getEditMessageText(
                callbackQuery,
                "Успешно! Запись добавлена, теперь вы можете изменить заголовок или описание ",
                keyboardFactory.inlineKeyboardMarkup(
                        List.of("Изменить загаловок", "Изменить описание",
                                "Завершить создание"),
                        List.of(2, 1),
                        List.of(TIMETABLE_ADD_TITTLE_ + id, TIMETABLE_ADD_DESCRIPTION_ + id,
                                TIMETABLE_FINISH_ + id)
                )
        );
    }
    private BotApiMethod<?> editTittle(CallbackQuery callbackQuery, String[] splitData){
        String id = splitData[3];
        var user = userRepository.findUserByChatId(callbackQuery.getMessage().getChatId());
        var details = user.getUserDetails();
        user.setAction(Action.SENDING_TITTLE);
        details.setTimetableId(id);
        userDetailsRep.save(details);
        user.setUserDetails(details);
        userRepository.save(user);
        return methodFactory.getEditMessageText(
                callbackQuery,
                "Введите заголовок: ",
                keyboardFactory.inlineKeyboardMarkup(
                        List.of("Назад"),
                        List.of(1),
                        List.of(TIMETABLE_BACK_ + id)
                )
        );
    }
        private BotApiMethod<?> editDescription(CallbackQuery callbackQuery, String[] splitData){
        String id = splitData[3];
        var user = userRepository.findUserByChatId(callbackQuery.getMessage().getChatId());
        var details = user.getUserDetails();
        user.setAction(Action.SENDING_DESCRIPTION);
        details.setTimetableId(id);
        userDetailsRep.save(details);
        user.setUserDetails(details);
        userRepository.save(user);
        return methodFactory.getEditMessageText(
                callbackQuery,
                "Введите описание",
                keyboardFactory.inlineKeyboardMarkup(
                        List.of("Назад"),
                        List.of(1),
                        List.of(TIMETABLE_BACK_ + id)
                )
        );
    }

    private BotApiMethod<?> setTittle(Message message, User user){
        user.setAction(Action.FREE);
        userRepository.save(user);
        String id = user.getUserDetails().getTimetableId();
        var timetable = timetableRepository.findTimetableById(UUID.fromString(id));
        timetable.setTittle(message.getText());
        timetableRepository.save(timetable);
        return methodFactory.getSendMessage(
                message.getChatId(),
                "Вы можете завершить создание или же настроить описание и заголовок",
                keyboardFactory.inlineKeyboardMarkup(
                        List.of("Изменить загаловок", "Изменить описание",
                                "Завершить создание"),
                        List.of(2, 1),
                        List.of(TIMETABLE_ADD_TITTLE_ + id, TIMETABLE_ADD_DESCRIPTION_ + id,
                                TIMETABLE_FINISH_ + id)
                )
        );
    }
    private BotApiMethod<?> setDescription(Message message, User user){
        user.setAction(Action.FREE);
        userRepository.save(user);
        String id = user.getUserDetails().getTimetableId();
        var timetable = timetableRepository.findTimetableById(UUID.fromString(id));
        timetable.setDescription(message.getText());
        timetableRepository.save(timetable);
        return methodFactory.getSendMessage(
                message.getChatId(),
                "Вы можете завершить создание или же настроить описание и заголовок",
                keyboardFactory.inlineKeyboardMarkup(
                        List.of("Изменить загаловок", "Изменить описание",
                                "Завершить создание"),
                        List.of(2, 1),
                        List.of(TIMETABLE_ADD_TITTLE_ + id, TIMETABLE_ADD_DESCRIPTION_ + id,
                                TIMETABLE_FINISH_ + id)
        )
        );
    }
    private BotApiMethod<?> finish(CallbackQuery callbackQuery, String[] splitData, TelegramBot telegramBot){
        var timetable = timetableRepository.findTimetableById(UUID.fromString(splitData[2]));
        timetable.setInCreation(false);
        timetableRepository.save(timetable);
        try {
            telegramBot.execute(methodFactory.answerCallbackQuery(callbackQuery.getId(),
                    "Процесс создания записи в расписании успешно завершен"));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
        return methodFactory.getDeleteMessage(callbackQuery.getMessage().getChatId(),
                callbackQuery.getMessage().getMessageId());


    }

    private BotApiMethod<?> back(CallbackQuery callbackQuery, String[] splitData){
        String id = splitData[2];
        var user = userRepository.findUserByChatId(callbackQuery.getMessage().getChatId());
        user.setAction(Action.FREE);
        userRepository.save(user);
        return methodFactory.getEditMessageText(
                callbackQuery,
                "Вы можете настроить описание или заголовок",
                keyboardFactory.inlineKeyboardMarkup(
                        List.of("Изменить загаловок", "Изменить описание",
                                "Завершить создание"),
                        List.of(2, 1),
                        List.of(TIMETABLE_ADD_TITTLE_ + id, TIMETABLE_ADD_DESCRIPTION_ + id,
                                TIMETABLE_FINISH_ + id)
                )
        );
    }

    private BotApiMethod<?> removeFromWeekday(CallbackQuery callbackQuery, String splitData){
        Weekday weekday = Weekday.MONDAY;
        switch (splitData){
            case "2" -> weekday = Weekday.TUESDAY;
            case "3" -> weekday = Weekday.WEDNESDAY;
            case "4" -> weekday= Weekday.THURSDAY;
            case "5" -> weekday = Weekday.FRIDAY;
            case "6" -> weekday = Weekday.SATURDAY;
            case "7" -> weekday = Weekday.SUNDAY;
        }
        List<String> data = new ArrayList<>();
        List<String> text = new ArrayList<>();
        for (Timetable timetable : timetableRepository.findAllByUsersContainingAndWeekday(
                userRepository.findUserByChatId(callbackQuery.getMessage().getChatId()), weekday)){
            data.add(TIMETABLE_REMOVE_POS_ + timetable.getId() + "_" + splitData);
            text.add(timetable.getTittle() + " " + timetable.getHour() + " : " + timetable.getMinute());
        }
        data.add(TIMETABLE_REMOVE);
        text.add("Назад");
        return methodFactory.getEditMessageText(
                callbackQuery,
                "Выберите урок который хотите удалить",
                keyboardFactory.inlineKeyboardMarkup(
                        text,
                        List.of(1, 1),
                        data
                )
        );
    }

    public BotApiMethod<?> askConfirmation(CallbackQuery callbackQuery, String[] splitData){
        return methodFactory.getEditMessageText(
                callbackQuery,
                "Вы уверены что хотите удалить занятие?",
                keyboardFactory.inlineKeyboardMarkup(
                        List.of("Да", "Нет"),
                        List.of(2),
                        List.of(TIMETABLE_REMOVE_CONFIRM_ + splitData[3] , TIMETABLE_REMOVE_WEEKDAY_ + splitData[4])
                )
        );
    }

    public BotApiMethod<?> removeLesson(CallbackQuery callbackQuery, String splitData, TelegramBot bot){
        System.out.println(callbackQuery.getData());
        var timetable = timetableRepository.findTimetableById(UUID.fromString(splitData));
        timetable.setUsers(null);
        timetableRepository.delete(timetable);
        try {
            bot.execute(methodFactory.getSendMessage(
                    callbackQuery.getMessage().getChatId(),
                    "Занятие успешно удалена!",
                    null
            ));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
        return methodFactory.getDeleteMessage(
                callbackQuery.getMessage().getChatId(),
                callbackQuery.getMessage().getMessageId());
    }
    private BotApiMethod<?> remove(CallbackQuery callbackQuery) {
        List<String> data = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            data.add(TIMETABLE_REMOVE_WEEKDAY_ + i);
        }
        data.add(TIMETABLE);
        return methodFactory.getEditMessageText(
                callbackQuery,
                """
                        ✂️ Выберите день из которого хотите удалить занятие
                        """,
                keyboardFactory.inlineKeyboardMarkup(
                        List.of("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс", "Назад"),
                        List.of(7, 1),
                        data
                )
        );
    }
}
