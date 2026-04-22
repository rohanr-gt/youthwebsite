package com.example.demo.repository;

import com.example.demo.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByCategory(String category);
    List<Event> findByStatusOrderByCreatedAtDesc(String status);
    List<Event> findByStatusInOrderByCreatedAtDesc(java.util.List<String> statuses);
    List<Event> findAllByOrderByCreatedAtDesc();
    long countByStatus(String status);

    // Search by venue or title (case-insensitive) — used for location search
    @Query("SELECT e FROM Event e WHERE LOWER(e.venue) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Event> searchByVenueOrTitle(@Param("keyword") String keyword);
}
