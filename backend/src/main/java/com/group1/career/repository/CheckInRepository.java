package com.group1.career.repository;

import com.group1.career.model.entity.CheckIn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CheckInRepository extends JpaRepository<CheckIn, Long> {

    Optional<CheckIn> findByUserIdAndDayAndAction(Long userId, LocalDate day, String action);

    List<CheckIn> findByUserIdAndDayBetweenOrderByDayAsc(Long userId, LocalDate from, LocalDate to);

    /** Distinct days the user checked in within [from, to] (inclusive). */
    @Query("select distinct c.day from CheckIn c " +
            "where c.userId = :userId and c.day between :from and :to " +
            "order by c.day desc")
    List<LocalDate> distinctDaysInRange(@Param("userId") Long userId,
                                        @Param("from") LocalDate from,
                                        @Param("to") LocalDate to);
}
