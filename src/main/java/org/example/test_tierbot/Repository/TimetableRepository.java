package org.example.test_tierbot.Repository;

import org.example.test_tierbot.Entity.User;
import org.example.test_tierbot.Entity.timetable.Timetable;
import org.example.test_tierbot.Entity.timetable.Weekday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TimetableRepository extends JpaRepository<Timetable, UUID> {
    List<Timetable> findAllByUsersContainingAndWeekday(User user, Weekday weekday);
    Timetable findTimetableById(UUID id);
}
