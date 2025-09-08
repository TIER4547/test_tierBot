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
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

import static org.example.test_tierbot.Service.data.CallBackData.AUTH_STUDENT;
import static org.example.test_tierbot.Service.data.CallBackData.AUTH_TEACHER;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class AuthManager extends AbstractManager{

    final UserRepository userRepository;

    final AnswerMethodFactory answerMethodFactory;

    final KeyboardFactory keyboardFactory;

    public AuthManager(UserRepository userRepository, AnswerMethodFactory answerMethodFactory, KeyboardFactory keyboardFactory) {
        this.userRepository = userRepository;
        this.answerMethodFactory = answerMethodFactory;
        this.keyboardFactory = keyboardFactory;
    }

    @Override
    public BotApiMethod<?> answerCommand(Message message, TelegramBot bot) {
        return null;
    }

    @Override
    public BotApiMethod<?> answerCallbackQuery(CallbackQuery callbackQuery, TelegramBot bot) {
        Long chatId = callbackQuery.getMessage().getChatId();
        int messageId = callbackQuery.getMessage().getMessageId();
        var user = userRepository.findById(chatId).orElseThrow();
        if (AUTH_TEACHER.equals(callbackQuery.getData())){
            user.setRole(Role.TEACHER);
        }
        else {
            user.setRole(Role.STUDENT);
        }
        user.setAction(Action.FREE);
        userRepository.save(user);

        try {
           bot.execute(answerMethodFactory.answerCallbackQuery(
                   callbackQuery.getId(),
                   """
                           Авторизация прошла успешно!
                           """
           ));
        } catch (TelegramApiException e){
            log.error(e.getMessage());
        }
        return answerMethodFactory.getDeleteMessage(chatId,messageId);
    }

    @Override
    public BotApiMethod<?> answerMessage(Message message, TelegramBot bot) {
        Long chatId = message.getChatId();
        User user = userRepository.findById(chatId).orElseThrow();
        user.setAction(Action.AUTH);
        userRepository.save(user);

        return answerMethodFactory.getSendMessage(chatId,
                """
                        Выберите свою роль
                        """,
                keyboardFactory.inlineKeyboardMarkup(
                List.of("Учитель", "Ученик"),
                List.of(2),
                List.of(AUTH_TEACHER, AUTH_STUDENT)
                )
        );
    }
}
