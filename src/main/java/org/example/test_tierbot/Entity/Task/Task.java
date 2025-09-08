package org.example.test_tierbot.Entity.Task;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.test_tierbot.Entity.User;

import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Task")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class Task {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(name = "title")
    String title;

    @Column(name = "text_Content")
    String textContent;

    @Column(name = "message_id")
    Integer messageId;

    @Column(name = "in_Creation")
    Boolean isInCreation;

    @ManyToMany
    @JoinTable(joinColumns = @JoinColumn(name = "task_id"),
                inverseJoinColumns = @JoinColumn(name = "user_id"),
                name = "tasks_teacher_student")
    List <User> users;
}
