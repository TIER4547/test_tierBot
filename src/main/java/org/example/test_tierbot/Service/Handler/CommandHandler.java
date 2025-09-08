package org.example.test_tierbot.Service.Handler;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.example.test_tierbot.Service.Factory.KeyboardFactory;
import org.example.test_tierbot.Service.Manager.*;
import org.example.test_tierbot.Service.Manager.progress_control.ProgressControlManager;
import org.example.test_tierbot.TelegramBot.TelegramBot;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;

import static org.example.test_tierbot.Service.data.Command.*;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommandHandler {
    final FeedBackManager feedBackManager;
    final HelpManager helpManager;
    final StartManager startManager;
    final TaskManager taskManager;
    final TimetableManager timetableManager;
    final ProgressControlManager progressControlManager;
    final ProfileManager profileManager;
    final SearchManager searchManager;

    public CommandHandler(FeedBackManager feedBackManager, HelpManager helpManager, StartManager startManager, TaskManager taskManager, TimetableManager timetableManager, ProgressControlManager progressControlManager, ProfileManager profileManager, SearchManager searchManager) {
        this.feedBackManager = feedBackManager;
        this.helpManager = helpManager;
        this.startManager = startManager;
        this.taskManager = taskManager;
        this.timetableManager = timetableManager;
        this.progressControlManager = progressControlManager;
        this.profileManager = profileManager;
        this.searchManager = searchManager;
    }

    public BotApiMethod<?> answer(Message message, TelegramBot telegramBot){
        String command = message.getText();
        switch (command){
            case START -> {
                return startManager.answerCommand(message, telegramBot);
            }
            case FEEDBACK_COMMAND -> {
                return feedBackManager.answerCommand(message, telegramBot);
            }
            case HELP_COMMAND -> {
                return helpManager.answerCommand(message, telegramBot);
            }
            case TASK_COMMAND -> {
                return taskManager.answerCommand(message, telegramBot);
            }
            case TIMETABLE_COMMAND -> {
                return timetableManager.answerCommand(message, telegramBot);
            }
            case PROGRESS -> {
                return progressControlManager.answerCommand(message, telegramBot);
            }
            case PROFILE ->{
                return profileManager.answerCommand(message, telegramBot);
            }
            case SEARCH -> {
                return searchManager.answerCommand(message, telegramBot);
            }
            default -> {
                return defaultmesage(message);
            }
        }
    }

    private BotApiMethod<?> defaultmesage(Message message) {
        return SendMessage.builder()
                .chatId(message.getChatId())
                .text("Неподдерживаемая команда")
                .build();
    }

}
