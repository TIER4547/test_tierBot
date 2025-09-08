package org.example.test_tierbot.Entity;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "Users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {

    @Id
    @Column(name = "id")
    Long chatId;

    @Column(name = "token")
    String token;

    @Enumerated(EnumType.STRING)
    Role role;

    @Enumerated(EnumType.STRING)
    Action action;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_details_id")
    UserDetails userDetails;


    @ManyToMany
    @JoinTable(
            joinColumns = @JoinColumn(name = "teacher_id"),
            inverseJoinColumns = @JoinColumn(name = "student_id"),
            name = "relationships"
    )
    List<User> users;

    @PrePersist
    private void generateUniqueToken(){
        if (token == null){
            token = UUID.randomUUID().toString();
        }
    }

    public void addUser(User user){
        if (users == null){
            users = new ArrayList<>();
        }
        users.add(user);
    }

    public void refreshToken(){
        token = UUID.randomUUID().toString();
    }

}
