package org.example.test_tierbot.Service.Manager.progress_control;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.example.test_tierbot.Service.Factory.AnswerMethodFactory;
import org.example.test_tierbot.Service.Factory.KeyboardFactory;
import org.example.test_tierbot.Service.Manager.AbstractManager;
import org.example.test_tierbot.TelegramBot.TelegramBot;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;

import static org.example.test_tierbot.Service.data.CallBackData.*;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProgressControlManager extends AbstractManager {

    final AnswerMethodFactory methodFactory;
    final KeyboardFactory keyboardFactory;

    public ProgressControlManager(AnswerMethodFactory methodFactory, KeyboardFactory keyboardFactory) {
        this.methodFactory = methodFactory;
        this.keyboardFactory = keyboardFactory;
    }

    @Override
    public BotApiMethod<?> answerCommand(Message message, TelegramBot bot) {
        return mainMenu(message);
    }

    @Override
    public BotApiMethod<?> answerCallbackQuery(CallbackQuery callbackQuery, TelegramBot bot) {
        String data = callbackQuery.getData();
        switch (data){
            case PROGRESS -> {
                return mainMenu(callbackQuery);
            }
            case PROGRESS_STAT -> {
                return stat(callbackQuery);
            }
        }
        return null;
    }

    @Override
    public BotApiMethod<?> answerMessage(Message message, TelegramBot bot) {
        return null;
    }

    private BotApiMethod<?> mainMenu(Message message){
        return methodFactory.getSendMessage(
                message.getChatId(),
                """ 
                        Здесь вы можете увидеть
                        """,
                keyboardFactory.inlineKeyboardMarkup(
                        List.of("Посмотреть успеваемость"),
                        List.of(1),
                        List.of(PROGRESS_STAT)
                )
        );
    }

    private BotApiMethod<?> mainMenu(CallbackQuery callbackQuery){
        return methodFactory.getEditMessageText(
                callbackQuery,
                """
                        Здесь вы можете увидеть
                        """,
                keyboardFactory.inlineKeyboardMarkup(
                        List.of("Посмотреть успеваемость"),
                        List.of(1),
                        List.of(PROGRESS_STAT)
                )
        );
    }

    private BotApiMethod<?> stat(CallbackQuery callbackQuery){
        return methodFactory.getEditMessageText(
                callbackQuery,
                """
                        Ваша успеваемость
                        """,
                keyboardFactory.inlineKeyboardMarkup(
                        List.of("Назад"),
                        List.of(1),
                        List.of(PROGRESS)
                )
        );
    }
}
