package org.example.test_tierbot.Service.Manager;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.example.test_tierbot.Service.Factory.AnswerMethodFactory;
import org.example.test_tierbot.Service.Factory.KeyboardFactory;
import org.example.test_tierbot.TelegramBot.TelegramBot;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FeedBackManager extends AbstractManager {

    final AnswerMethodFactory answerMethodFactory;

    final KeyboardFactory keyboardFactory;

    public FeedBackManager(AnswerMethodFactory answerMethodFactory, KeyboardFactory keyboardFactory) {
        this.answerMethodFactory = answerMethodFactory;
        this.keyboardFactory = keyboardFactory;
    }

    public BotApiMethod<?> answerCommand(Message message, TelegramBot bot) {
        return answerMethodFactory.getSendMessage(
                    message.getChatId(),
                    """
                        📍 Ссылки для обратной связи
                        GitHub - https://github.com/pavelitel05
                        LinkedIn - https://linkedin.com/in/павел-кирсанов-62b762263
                        Telegram - https://t.me/pavelitel05
                        """,
                        null
                );
    }

    public BotApiMethod<?> answerCallbackQuery(CallbackQuery callbackQuery, TelegramBot bot) {
        return answerMethodFactory.getEditMessageText(
                callbackQuery,
                """
                        📍 Ссылки для обратной связи
                        GitHub - https://github.com/pavelitel05
                        LinkedIn - https://linkedin.com/in/павел-кирсанов-62b762263
                        Telegram - https://t.me/pavelitel05
                        """,
                null
        );
    }

    @Override
    public BotApiMethod<?> answerMessage(Message message, TelegramBot bot) {
        return null;
    }
}
