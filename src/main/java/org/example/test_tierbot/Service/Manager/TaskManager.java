package org.example.test_tierbot.Service.Manager;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.test_tierbot.Entity.Action;
import org.example.test_tierbot.Entity.Task.Task;
import org.example.test_tierbot.Entity.User;
import org.example.test_tierbot.Repository.TaskRepository;
import org.example.test_tierbot.Repository.UserRepository;
import org.example.test_tierbot.Service.Factory.AnswerMethodFactory;
import org.example.test_tierbot.Service.Factory.KeyboardFactory;
import org.example.test_tierbot.TelegramBot.TelegramBot;
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

@Slf4j
@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaskManager extends AbstractManager{

    final AnswerMethodFactory answerMethodFactory;

    final KeyboardFactory keyboardFactory;
    final UserRepository userRepository;
    final TaskRepository taskRepository;

    public TaskManager(AnswerMethodFactory answerMethodFactory, KeyboardFactory keyboardFactory, UserRepository userRepository, TaskRepository taskRepository) {
        this.answerMethodFactory = answerMethodFactory;
        this.keyboardFactory = keyboardFactory;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
    }

    @Override
    public BotApiMethod<?> answerCommand(Message message, TelegramBot bot) {
        return mainMenu(message);
    }

    @Transactional
    @Override
    public BotApiMethod<?> answerCallbackQuery(CallbackQuery callbackQuery, TelegramBot bot) {
        String query = callbackQuery.getData();
        switch (query){
            case TASK_CREATE -> {
                return create(callbackQuery);
            }
            case TASK -> {
                return mainMenu(callbackQuery);
            }
        }
        String[] splitData = query.split("_");
        if (MENU.equals(splitData[1])){
            return menu(callbackQuery, splitData[2]);
        }
        if (splitData.length > 2){
            String keyWord = splitData[2];
        switch (keyWord) {
            case USER -> {
                return setTaskToUser(callbackQuery, splitData);
            }
            case CANCEL -> {
                return cancelTask(callbackQuery, splitData[3], bot);
            }
            case TEXT ->
            {
                return editText(callbackQuery, splitData, bot);
            }
            case SEND ->{
                return askConfirmation(callbackQuery, splitData);
            }
            case CONFIRM -> {
                return sendTask(callbackQuery, splitData, bot);
            }
        }
        }
        return null;
    }

    @Override
    public BotApiMethod<?> answerMessage(Message message, TelegramBot bot) {
        var chatId = message.getChatId();
        var user = userRepository.findUserByChatId(chatId);
        switch (user.getAction()){
            case SENDING_TASKS -> {
                return addTask(message, chatId, user);
            }case EDIT_TASKS ->
            {
                return setEditText(message, chatId, user);
            }
        }
        return null;
    }

    private BotApiMethod<?> mainMenu(Message message){
        return answerMethodFactory.getSendMessage(message.getChatId(),
                """ 
                        🗂 Вы можете добавить домашнее задание вашему ученику
                        """,
                keyboardFactory.inlineKeyboardMarkup(
                        List.of("Прикрепить домашнее задание"),
                        List.of(1),
                        List.of(TASK_CREATE)
                ));
    }

    private BotApiMethod<?> mainMenu(CallbackQuery callbackQuery){
        return answerMethodFactory.getEditMessageText(callbackQuery,
                """
                         🗂 Вы можете добавить домашнее задание вашему ученику
                        """,
                keyboardFactory.inlineKeyboardMarkup(
                        List.of("Прикрепить домашнее задание"),
                        List.of(1),
                        List.of(TASK_CREATE)
                ));
    }
    private BotApiMethod<?> create(CallbackQuery callbackQuery){
        List<String> text = new ArrayList<>();
        List<String> data = new ArrayList<>();
        List<Integer> cfg = new ArrayList<>();

        var teacher = userRepository.findUserByChatId(callbackQuery.getMessage().getChatId());
        int index = 0;
        for (User student : teacher.getUsers()){
            text.add(student.getUserDetails().getFirstName());
            data.add(TASK_CREATE_USER_ + student.getChatId());
            if (index == 4){
                cfg.add(index);
            }
            else {
                index++;
            }
        }
        if (index != 0){
            cfg.add(index);
        }

        text.add("");
        data.add(TASK);
        cfg.add(1);

        return answerMethodFactory.getEditMessageText(
                callbackQuery,
                """
                        👤 Выберете ученика, которому хотите дать домашнее задание
                        """,
                keyboardFactory.inlineKeyboardMarkup(
                        text,
                        cfg,
                        data
                ));
    }

    private BotApiMethod<?> setTaskToUser(CallbackQuery callbackQuery, String[] splitData){
        var user = userRepository.findUserByChatId(callbackQuery.getMessage().getChatId());
        taskRepository.deleteByUsersContainingAndIsInCreation(user, true);
        taskRepository.save(Task.builder()
                        .users(List.of(
                                userRepository.findUserByChatId(Long.valueOf(splitData[3])),
                                user))
                        .isInCreation(true)
                .build());
        user.setAction(Action.SENDING_TASKS);
        return answerMethodFactory.getEditMessageText(
                callbackQuery,
                """
                       Отправьте задание одним сообщением, позже вы сможете его изменить """,
                keyboardFactory.inlineKeyboardMarkup(
                        List.of("Назад"),
                        List.of(1),
                        List.of(TASK_CREATE)
                        )
                );
    }
    private BotApiMethod<?> addTask(Message message, Long chatId, User user){
        Task task = taskRepository.findTaskByUsersContainingAndIsInCreation(user, true);
        task.setMessageId(message.getMessageId());
        task.setTitle(message.getText());
        taskRepository.save(task);
        String id = String.valueOf(task.getId());
        user.setAction(Action.FREE);
        userRepository.save(user);
        return answerMethodFactory.getSendMessage(
                chatId,
                message.getText() +  "\nНастройте ваше задание, когда будете готовы- жмите \"Отправить\"",
                keyboardFactory.inlineKeyboardMarkup(
                        List.of("Изменить текст", "Изменить медиа", "Выбрать ученика", "Отправить", "Отмена"),
                        List.of(2, 1, 1, 1),
                        List.of(TASK_CREATE_TEXT_ + id, TASK_CREATE_MEDIA + id,
                                TASK_CREATE_CHANGE_USER_ + id,
                                TASK_CREATE_SEND_ + id,
                                TASK_CREATE_CANCEL_ + id)
                )
                );
    }
    private BotApiMethod<?> editText(CallbackQuery callbackQuery, String[] splitData, TelegramBot bot){
        var user = userRepository.findUserByChatId(callbackQuery.getMessage().getChatId());
        user.setAction(Action.EDIT_TASKS);
        userRepository.save(user);
        try {
            bot.execute(answerMethodFactory.getDeleteMessage(callbackQuery.getMessage().getChatId(), callbackQuery.getMessage().getMessageId()));
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
        return answerMethodFactory.getSendMessage(
                callbackQuery.getMessage().getChatId(),
                "Отправьте измененный текст задания",
                null
                );
    }
    private BotApiMethod<?> setEditText(Message message, Long chatId, User user){
        var newTask = taskRepository.findTaskByUsersContainingAndIsInCreation(user, true);
        newTask.setTitle(message.getText());
        var taskId = newTask.getId();
        user.setAction(Action.FREE);
        userRepository.save(user);
        return answerMethodFactory.getSendMessage(
                chatId,
                message.getText() +  "\nНастройте ваше задание, когда будете готовы- жмите \"Отправить\"",
                keyboardFactory.inlineKeyboardMarkup(
                        List.of("Изменить текст", "Изменить медиа", "Выбрать ученика", "Отправить", "Отмена"),
                        List.of(2, 1, 1, 1),
                        List.of(TASK_CREATE_TEXT_ + taskId, TASK_CREATE_MEDIA + taskId,
                                TASK_CREATE_CHANGE_USER_ + taskId,
                                TASK_CREATE_SEND_ + taskId,
                                TASK_CREATE_CANCEL_ + taskId)
                )
        );
    }

    private BotApiMethod<?> askConfirmation(CallbackQuery callbackQuery, String[] splitData){
        return answerMethodFactory.getEditMessageText(
                callbackQuery,
                "Вы уверены что хотите отправить задание?",
                keyboardFactory.inlineKeyboardMarkup(
                        List.of("да", "Нет"),
                        List.of(2),
                        List.of(TASK_CREATE_CONFIRM_ + splitData[3], TASK_MENU_ + splitData[3])
                )
        );
    }
    private BotApiMethod<?> menu(CallbackQuery callbackQuery, String splitData){
        return answerMethodFactory.getEditMessageText(
                callbackQuery,
                "\nНастройте ваше задание, когда будете готовы- жмите \"Отправить\"",
                keyboardFactory.inlineKeyboardMarkup(
                        List.of("Изменить текст", "Изменить медиа", "Выбрать ученика", "Отправить", "Отмена"),
                        List.of(2, 1, 1, 1),
                        List.of(TASK_CREATE_TEXT_ + splitData, TASK_CREATE_MEDIA + splitData,
                                TASK_CREATE_CHANGE_USER_ + splitData,
                                TASK_CREATE_SEND_ + splitData,
                                TASK_CREATE_CANCEL_ + splitData)
                )
        );
    }

    private BotApiMethod<?> sendTask(CallbackQuery callbackQuery, String[] splitData, TelegramBot bot){
        var user = userRepository.findUserByChatId(callbackQuery.getMessage().getChatId());
        var task = taskRepository.findTaskByUsersContainingAndIsInCreation(user, true);
        task.setIsInCreation(false);
        taskRepository.save(task);
        try {
            bot.execute(answerMethodFactory.answerCallbackQuery(
                    callbackQuery.getId(),
                    "Задание успешно отправлено"
            ));
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
        return answerMethodFactory.getDeleteMessage(callbackQuery.getMessage().getChatId(), callbackQuery.getMessage().getMessageId());
    }

    private BotApiMethod<?> cancelTask(CallbackQuery callbackQuery, String splitdata, TelegramBot bot){
        taskRepository.deleteById(UUID.fromString(splitdata));
        try {
            bot.execute(answerMethodFactory.answerCallbackQuery(callbackQuery.getId(), "Операция успешно отменена!"));
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
        return answerMethodFactory.getDeleteMessage(callbackQuery.getMessage().getChatId(), callbackQuery.getMessage().getMessageId());
    }
}
