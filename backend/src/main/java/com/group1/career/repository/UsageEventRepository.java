package com.group1.career.repository;

import com.group1.career.model.entity.UsageEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UsageEventRepository extends JpaRepository<UsageEvent, Long> {

    @Query("SELECT e.eventType, COUNT(e) FROM UsageEvent e " +
           "WHERE e.createdAt >= :since GROUP BY e.eventType ORDER BY COUNT(e) DESC")
    List<Object[]> countByEventTypeSince(LocalDateTime since);

    @Query("SELECT DATE(e.createdAt), COUNT(DISTINCT e.userId) FROM UsageEvent e " +
           "WHERE e.createdAt >= :since AND e.eventType = :eventType GROUP BY DATE(e.createdAt)")
    List<Object[]> dailyActiveUsersSince(String eventType, LocalDateTime since);

    long countByEventTypeAndCreatedAtAfter(String eventType, LocalDateTime after);
}
