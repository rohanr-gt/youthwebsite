package com.example.demo.repository;

import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
    List<User> findByUsernameContainingIgnoreCase(String username);
    List<User> findAllByOrderByXpDesc();

    // Search by college name (case-insensitive)
    List<User> findByCollegeNameContainingIgnoreCase(String collegeName);

    // Search by both name AND college
    @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%',:name,'%')) AND LOWER(u.collegeName) LIKE LOWER(CONCAT('%',:college,'%'))")
    List<User> findByUsernameAndCollege(@Param("name") String name, @Param("college") String college);

    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query(value = "DELETE FROM user_voted_events WHERE event_id = ?1", nativeQuery = true)
    void deleteFromVotedEvents(Long eventId);
}
