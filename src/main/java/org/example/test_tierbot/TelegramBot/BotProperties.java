package org.example.test_tierbot.TelegramBot;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@ConfigurationProperties("bot")
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BotProperties {
    String token;

    String username;

    String path;
}
