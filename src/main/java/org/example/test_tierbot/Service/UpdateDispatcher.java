package org.example.test_tierbot.Service;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.test_tierbot.Entity.User;
import org.example.test_tierbot.Repository.UserRepository;
import org.example.test_tierbot.Service.Handler.CallbackQueryHandler;
import org.example.test_tierbot.Service.Handler.CommandHandler;
import org.example.test_tierbot.Service.Handler.MessageHandler;
import org.example.test_tierbot.TelegramBot.TelegramBot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class UpdateDispatcher {


    final CallbackQueryHandler callbackQueryHandler;
    final CommandHandler commandHandler;
    final MessageHandler messageHandler;

    @Autowired
    public UpdateDispatcher(CallbackQueryHandler callbackQueryHandler, CommandHandler commandHandler, MessageHandler messageHandler) {
        this.callbackQueryHandler = callbackQueryHandler;
        this.commandHandler = commandHandler;
        this.messageHandler = messageHandler;
    }

    public BotApiMethod<?> distribute(Update update, TelegramBot telegramBot){
        if (update.hasMessage()){
            Message message = update.getMessage();
            if (message.getText().charAt(0) == '/' ){
                return commandHandler.answer(update.getMessage(), telegramBot);
            }
            if (update.getMessage().hasText()){
                return messageHandler.answer(update.getMessage(), telegramBot);
            }
        }
        if (update.hasCallbackQuery()){
            return callbackQueryHandler.answer(update.getCallbackQuery(), telegramBot);
        }
        log.info("Unsupported update type");
        return null;
    }
}
