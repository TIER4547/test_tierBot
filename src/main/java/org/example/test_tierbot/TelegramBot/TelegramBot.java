package org.example.test_tierbot.TelegramBot;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.example.test_tierbot.Service.UpdateDispatcher;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TelegramBot extends TelegramWebhookBot {

    final UpdateDispatcher updateDispatcher;

    final BotProperties botProperties;

    public TelegramBot(UpdateDispatcher updateDispatcher, BotProperties botProperties) {
        super(botProperties.getToken());
        this.updateDispatcher = updateDispatcher;
        this.botProperties = botProperties;
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        return updateDispatcher.distribute(update, this);
    }

    @Override
    public String getBotPath() {
        return botProperties.getPath();
    }

    @Override
    public String getBotUsername() {
        return botProperties.getUsername();
    }
}
