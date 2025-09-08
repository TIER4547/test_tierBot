package org.example.test_tierbot.Repository;

import org.example.test_tierbot.Entity.User;
import org.example.test_tierbot.Entity.UserDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserDetailsRep extends JpaRepository<UserDetails, UUID> {
}
