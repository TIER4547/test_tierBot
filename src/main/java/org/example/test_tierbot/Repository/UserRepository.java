package org.example.test_tierbot.Repository;

import org.example.test_tierbot.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findUserByChatId(Long chatId);
    User findUserByToken(String token);
    List<User> findAllByUsersContaining(User user);
}
