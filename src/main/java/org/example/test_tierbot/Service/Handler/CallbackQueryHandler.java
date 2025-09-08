package org.example.test_tierbot.Service.Handler;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.example.test_tierbot.Service.Manager.*;
import org.example.test_tierbot.Service.Manager.progress_control.ProgressControlManager;
import org.example.test_tierbot.TelegramBot.TelegramBot;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import static org.example.test_tierbot.Service.data.CallBackData.*;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CallbackQueryHandler {

    final FeedBackManager feedBackManager;

    final HelpManager helpManager;
    final TaskManager taskManager;
    final TimetableManager timetableManager;
    final ProgressControlManager progressControlManager;
    final AuthManager authManager;
    final SearchManager searchManager;
    final ProfileManager profileManager;

    public CallbackQueryHandler(FeedBackManager feedBackManager, HelpManager helpManager, TaskManager taskManager, TimetableManager timetableManager, ProgressControlManager progressControlManager, AuthManager authManager, SearchManager searchManager, ProfileManager profileManager) {
        this.feedBackManager = feedBackManager;
        this.helpManager = helpManager;
        this.taskManager = taskManager;
        this.timetableManager = timetableManager;
        this.progressControlManager = progressControlManager;
        this.authManager = authManager;
        this.searchManager = searchManager;
        this.profileManager = profileManager;
    }


    public BotApiMethod<?> answer(CallbackQuery callbackQuery,TelegramBot telegrambot){
        String callbackData = callbackQuery.getData();
        String keyword = callbackData.split("_")[0];
        if (TASK.equals(keyword)){
            return taskManager.answerCallbackQuery(callbackQuery, telegrambot);
        }
        if (TIMETABLE.equals(keyword)){
            return timetableManager.answerCallbackQuery(callbackQuery, telegrambot);
        }
        if (PROGRESS.equals(keyword)){
            return progressControlManager.answerCallbackQuery(callbackQuery, telegrambot);
        }
        if (AUTH.equals(keyword)){
            return authManager.answerCallbackQuery(callbackQuery, telegrambot);
        }
        if (SEARCH.equals(keyword)){
            return searchManager.answerCallbackQuery(callbackQuery, telegrambot);
        }
        if (REFRESH.equals(keyword)){
            return profileManager.answerCallbackQuery(callbackQuery, telegrambot);
        }
        switch (callbackData){
            case HELP -> {
                return helpManager.answerCallbackQuery(callbackQuery , telegrambot);
            }
            case FEEDBACK -> {
                return feedBackManager.answerCallbackQuery(callbackQuery, telegrambot);
            }
        }
        return null;
    }
}
