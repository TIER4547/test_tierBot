package org.example.test_tierbot.Entity.timetable;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.test_tierbot.Entity.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Timetable")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class Timetable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(name = "tittle")
    String tittle;

    @Column(name = "description")
    String description;

    @Enumerated(EnumType.STRING)
    Weekday weekday;

    @Column(name = "hour")
    Short hour;

    @Column(name = "minute")
    Short minute;

    @Column(name = "inCreation")
    boolean inCreation;

    @ManyToMany
    @JoinTable(
            joinColumns = @JoinColumn(name = "timetable_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"),
            name = "users_timetable")
    List<User> users;

    public void addUser(User user) {
        if (users == null) {
            users = new ArrayList<>();
        }
        users.add(user);
    }
}
