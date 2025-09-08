package org.example.test_tierbot.Service.Manager;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.example.test_tierbot.Service.Factory.AnswerMethodFactory;
import org.example.test_tierbot.Service.Factory.KeyboardFactory;
import org.example.test_tierbot.TelegramBot.TelegramBot;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;

import static org.example.test_tierbot.Service.data.CallBackData.*;
import static org.example.test_tierbot.Service.data.Command.*;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StartManager extends AbstractManager {

    final AnswerMethodFactory answerMethodFactory;

    final KeyboardFactory keyboardFactory;

    public StartManager(AnswerMethodFactory answerMethodFactory, KeyboardFactory keyboardFactory) {
        this.answerMethodFactory = answerMethodFactory;
        this.keyboardFactory = keyboardFactory;
    }

    public BotApiMethod<?> answerCommand(Message message, TelegramBot bot){
        return answerMethodFactory.getSendMessage(
                message.getChatId(),
                """
                        👋 Приветствую в Tutor-Bot, инструменте для упрощения взаимодействия репетитора и ученика.
                                                
                        Что бот умеет?
                        📌 Составлять расписания
                        📌 Прикреплять домашние задания
                        📌 Вести контроль успеваемости
                        """,
                keyboardFactory.inlineKeyboardMarkup(
                        List.of("Помощь", "Обратная свзяь"),
                        List.of(2),
                        List.of(HELP, FEEDBACK)
                )
        );
    }

    @Override
    public BotApiMethod<?> answerCallbackQuery(CallbackQuery callbackQuery, TelegramBot bot) {
        return null;
    }

    @Override
    public BotApiMethod<?> answerMessage(Message message, TelegramBot bot) {
        return null;
    }
}
