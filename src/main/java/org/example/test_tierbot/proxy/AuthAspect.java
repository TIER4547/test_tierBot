package org.example.test_tierbot.proxy;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.example.test_tierbot.Entity.Action;
import org.example.test_tierbot.Entity.Role;
import org.example.test_tierbot.Entity.User;
import org.example.test_tierbot.Repository.UserRepository;
import org.example.test_tierbot.Service.Manager.AuthManager;
import org.example.test_tierbot.TelegramBot.TelegramBot;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Aspect
@Component
@Order(100)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthAspect {

    final UserRepository userRepository;

    final AuthManager authManager;

    public AuthAspect(UserRepository userRepository, AuthManager authManager) {
        this.userRepository = userRepository;
        this.authManager = authManager;
    }

    @Pointcut("execution(* org.example.test_tierbot.Service.UpdateDispatcher.distribute(..))")
    public void distributeMethodPointcut(){}

    @Around("distributeMethodPointcut()")
    public Object authMethodAdvice(ProceedingJoinPoint joinPoint) throws Throwable{
        Update update = (Update) joinPoint.getArgs()[0];
        User user;
        if (update.hasMessage()){
            user = userRepository.findById(update.getMessage().getChatId()).orElseThrow();
        } else if (update.hasCallbackQuery()) {
            user = userRepository.findById(update.getCallbackQuery().getMessage().getChatId()).orElseThrow();
        }
        else {
            return joinPoint.proceed();
        }

        if (user.getRole() != Role.GUEST){
            return joinPoint.proceed();
        }

        if (user.getAction() == Action.AUTH){
            return joinPoint.proceed();
        }
        return authManager.answerMessage(update.getMessage(),
                (TelegramBot) joinPoint.getArgs()[1]);

        }

    }
