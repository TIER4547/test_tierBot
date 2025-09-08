package org.example.test_tierbot.Service.Handler;


import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.example.test_tierbot.Repository.UserRepository;
import org.example.test_tierbot.Service.Manager.SearchManager;
import org.example.test_tierbot.Service.Manager.TaskManager;
import org.example.test_tierbot.Service.Manager.TimetableManager;
import org.example.test_tierbot.TelegramBot.TelegramBot;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MessageHandler {

    final SearchManager searchManager;
    final UserRepository repository;
    final TimetableManager timetableManager;
    final TaskManager taskManager;

    public MessageHandler(SearchManager searchManager, UserRepository repository, TimetableManager timetableManager, TaskManager taskManager) {
        this.searchManager = searchManager;
        this.repository = repository;
        this.timetableManager = timetableManager;
        this.taskManager = taskManager;
    }

    public BotApiMethod<?> answer(Message message, TelegramBot telegramBot){
        var user = repository.findUserByChatId(message.getChatId());
        switch (user.getAction()){
            case SENDING_TOKEN -> {
                return searchManager.answerMessage(message, telegramBot);
            }
            case SENDING_TITTLE -> {
                return timetableManager.answerMessage(message, telegramBot);
            }
            case SENDING_DESCRIPTION -> {
                return timetableManager.answerMessage(message, telegramBot);
            }
            case SENDING_TASKS -> {
                return taskManager.answerMessage(message, telegramBot);
            }
            case EDIT_TASKS -> {
                return taskManager.answerMessage(message, telegramBot);
            }
        }
        return null;
    }
}
