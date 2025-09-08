package org.example.test_tierbot.Service.Manager;

import org.example.test_tierbot.TelegramBot.TelegramBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

public abstract class AbstractManager {

    public abstract BotApiMethod<?> answerCommand(Message message, TelegramBot bot);
    public abstract BotApiMethod<?> answerCallbackQuery(CallbackQuery callbackQuery, TelegramBot bot);
    public abstract BotApiMethod<?> answerMessage(Message message, TelegramBot bot);
}
