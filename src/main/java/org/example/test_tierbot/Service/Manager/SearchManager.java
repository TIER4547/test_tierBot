package org.example.test_tierbot.Service.Manager;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.test_tierbot.Entity.Action;
import org.example.test_tierbot.Entity.Role;
import org.example.test_tierbot.Entity.User;
import org.example.test_tierbot.Repository.UserRepository;
import org.example.test_tierbot.Service.Factory.AnswerMethodFactory;
import org.example.test_tierbot.Service.Factory.KeyboardFactory;
import org.example.test_tierbot.TelegramBot.TelegramBot;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

import static org.example.test_tierbot.Service.data.CallBackData.SEARCH_CANCEL;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class SearchManager extends AbstractManager{

    final UserRepository userRepository;
    final AnswerMethodFactory methodFactory;
    final KeyboardFactory keyboardFactory;

    public SearchManager(UserRepository userRepository, AnswerMethodFactory methodFactory, KeyboardFactory keyboardFactory) {
        this.userRepository = userRepository;
        this.methodFactory = methodFactory;
        this.keyboardFactory = keyboardFactory;
    }

    @Override
    public BotApiMethod<?> answerCommand(Message message, TelegramBot bot) {
        return askToken(message);
    }

    @Override
    public BotApiMethod<?> answerCallbackQuery(CallbackQuery callbackQuery, TelegramBot bot) {
        switch (callbackQuery.getData()){
            case SEARCH_CANCEL -> {
                return cancel(callbackQuery, bot);
            }
        }
        return null;
    }

    @Override
    public BotApiMethod<?> answerMessage(Message message, TelegramBot bot) {
        var user = userRepository.findUserByChatId(message.getChatId());
        switch (user.getAction()) {
            case SENDING_TOKEN -> {
                return checkToken(message, user);
            }
        }
        return null;
    }

    private BotApiMethod<?> askToken(Message message){
        Long chatId = message.getChatId();
        var user = userRepository.findUserByChatId(chatId);
        user.setAction(Action.SENDING_TOKEN);
        userRepository.save(user);
        return methodFactory.getSendMessage(
                chatId,
                "Отправьте токен",
                keyboardFactory.inlineKeyboardMarkup(
                        List.of("Отмена операции"),
                        List.of(1),
                        List.of(SEARCH_CANCEL)
                )
        );
    }

    private BotApiMethod<?> cancel(CallbackQuery callbackQuery, TelegramBot bot){
        Long chatId = callbackQuery.getMessage().getChatId();
        var user = userRepository.findUserByChatId(chatId);
        user.setAction(Action.FREE);
        userRepository.save(user);

        try {
            bot.execute(methodFactory.getSendMessage(
                    chatId,
                    "Операция выполнена успешно",
                    null
            ));
        } catch (TelegramApiException e) {
                 log.error(e.getMessage());
        }

        return methodFactory.getDeleteMessage(
                chatId,
                callbackQuery.getMessage().getMessageId()
        );
    }

    private BotApiMethod<?> checkToken(Message message, User user){

        String token = message.getText();
        var userTwo = userRepository.findUserByToken(token);
        if (userTwo == null){
            return methodFactory.getSendMessage(
                    message.getChatId(),
                    "По данному токену не найдено ни одного пользователя.\n Повторите попытку",
                    keyboardFactory.inlineKeyboardMarkup(
                            List.of("Отмена операции"),
                            List.of(1),
                            List.of(SEARCH_CANCEL)
                    )
            );
        }
        if (validation(user, userTwo)){
            if (user.getRole() == Role.TEACHER){
                user.addUser(userTwo);
            }
            else {
                userTwo.addUser(user);
            }
            user.setAction(Action.FREE);
            userRepository.save(user);
            userRepository.save(userTwo);
            return methodFactory.getSendMessage(
                    message.getChatId(),
                    "Связь успешно установлено",
                    null
            );
        }
        return methodFactory.getSendMessage(
                message.getChatId(),
                "Вы не можете установить соединение с учителем, если вы им являетесь, " +
                        "или то же самое, если вы ученик" +'\n' + "Повторите попытку!",
                keyboardFactory.inlineKeyboardMarkup(
                        List.of("Отмена операции"),
                        List.of(1),
                        List.of(SEARCH_CANCEL)
                )
        );


    }

    private boolean validation(User user, User userTwo){
        return user.getRole() != userTwo.getRole();
    }

}
