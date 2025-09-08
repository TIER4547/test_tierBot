package org.example.test_tierbot.proxy;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.example.test_tierbot.Entity.Action;
import org.example.test_tierbot.Entity.Role;
import org.example.test_tierbot.Entity.UserDetails;
import org.example.test_tierbot.Repository.UserDetailsRep;
import org.example.test_tierbot.Repository.UserRepository;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.time.LocalDateTime;

@Aspect
@Component
@Order(10)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreationAspect {

    UserRepository userRepository;

    UserDetailsRep userDetailsRep;

    public UserCreationAspect(UserRepository userRepository, UserDetailsRep userDetailsRep) {
        this.userRepository = userRepository;
        this.userDetailsRep = userDetailsRep;
    }


    @Pointcut("execution(* org.example.test_tierbot.Service.UpdateDispatcher.distribute(..))")
    public void distributeMethodPointcut(){}

    @Around("distributeMethodPointcut()")
    public Object distributeMethodAdvice(ProceedingJoinPoint joinPoint) throws Throwable{
        Object[] args = joinPoint.getArgs();
        Update update = (Update) args[0];
        User telegramUser;
        if (update.hasMessage()){
            telegramUser = update.getMessage().getFrom();
        }
        else if (update.hasCallbackQuery()){
            telegramUser = update.getCallbackQuery().getFrom();
        }
        else{
            return joinPoint.proceed();
        }

        if (userRepository.existsById(telegramUser.getId())){
            return joinPoint.proceed();
        }

        UserDetails userDetails = UserDetails.builder()
                .username(telegramUser.getUserName())
                .firstName(telegramUser.getFirstName())
                .lastName(telegramUser.getLastName())
                .registeredAt(LocalDateTime.now())
                .build();
        userDetailsRep.save(userDetails);

        org.example.test_tierbot.Entity.User newUser =
                org.example.test_tierbot.Entity.User.builder()
                        .chatId(telegramUser.getId())
                        .action(Action.FREE)
                        .role(Role.GUEST)
                        .userDetails(userDetails)
                        .build();
        userRepository.save(newUser);

        return joinPoint.proceed();
    }
}
