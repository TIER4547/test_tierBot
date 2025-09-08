package org.example.test_tierbot.Controller;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.example.test_tierbot.TelegramBot.TelegramBot;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BotController {

    final TelegramBot telegramBot;

    public BotController(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    @PostMapping("/")
    public BotApiMethod<?> listener(@RequestBody Update update){
//        if (update.hasMessage()){
//            return echo(update.getMessage());
//        }x`
        return telegramBot.onWebhookUpdateReceived(update);
    }

//    private BotApiMethod<?> echo(Message message){
//        return SendMessage.builder()
//                .chatId(message.getChatId())
//                .text(message.getText())
//                .build();
//    }
}
