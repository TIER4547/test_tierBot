package org.example.test_tierbot.Service.Manager;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
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

import java.security.SecureRandom;
import java.util.List;

import static org.example.test_tierbot.Service.data.CallBackData.REFRESH_TOKEN;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProfileManager extends AbstractManager{

    final UserRepository userRepository;
    final AnswerMethodFactory methodFactory;
    final KeyboardFactory keyboardFactory;

    public ProfileManager(UserRepository userRepository, AnswerMethodFactory methodFactory, KeyboardFactory keyboardFactory) {
        this.userRepository = userRepository;
        this.methodFactory = methodFactory;
        this.keyboardFactory = keyboardFactory;
    }

    @Override
    public BotApiMethod<?> answerCommand(Message message, TelegramBot bot) {
        return showProfile(message);
    }

    @Override
    public BotApiMethod<?> answerCallbackQuery(CallbackQuery callbackQuery, TelegramBot bot) {
        var user = userRepository.findUserByChatId(callbackQuery.getMessage().getChatId());
        switch (callbackQuery.getData()){
            case REFRESH_TOKEN -> {
                 user.refreshToken();
                 userRepository.save(user);
                 return showProfile(callbackQuery);
            }
        }
        return null;
    }

    @Override
    public BotApiMethod<?> answerMessage(Message message, TelegramBot bot) {
        return null;
    }

    private BotApiMethod<?> showProfile(CallbackQuery callbackQuery){
        return methodFactory.getEditMessageText(
                callbackQuery,
                getTextProfile(callbackQuery.getMessage()),
                keyboardFactory.inlineKeyboardMarkup(
                        List.of("Обновить токен"),
                        List.of(1),
                        List.of(REFRESH_TOKEN)
                )
        );
    }
    private BotApiMethod<?> showProfile(Message message){
        return methodFactory.getSendMessage(
                message.getChatId(),
                getTextProfile(message),
                keyboardFactory.inlineKeyboardMarkup(
                        List.of("Обновить токен"),
                        List.of(1),
                        List.of(REFRESH_TOKEN)
                )
        );
    }

    private String getTextProfile(Message message) {
        Long chatId = message.getChatId();
        StringBuilder text = new StringBuilder();
        var user = userRepository.findById(chatId).orElseThrow();
        var details = user.getUserDetails();
        if (details.getUsername() != null){
            text.append("▪\uFE0FИмя пользователя - " + details.getUsername());
        }
        else {
            text.append("▪\uFE0FИмя пользователя - " + details.getFirstName());
        }
        text.append("▪\n\uFE0FРоль - " + user.getRole());
        text.append("▪\n\uFE0FВаш уникальный токен - \n" + user.getToken());
        text.append("\n\n⚠\uFE0F - токен необходим для того, чтобы ученик или преподаватель могли установиться между собой связь");

        return text.toString();
    }
}
