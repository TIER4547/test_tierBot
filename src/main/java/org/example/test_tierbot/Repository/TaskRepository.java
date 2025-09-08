package org.example.test_tierbot.Repository;

import org.example.test_tierbot.Entity.Task.Task;
import org.example.test_tierbot.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
    void deleteByUsersContainingAndIsInCreation(User user, Boolean isInCreation);
    Task findTaskByUsersContainingAndIsInCreation(User user, Boolean isInCreation);
}
