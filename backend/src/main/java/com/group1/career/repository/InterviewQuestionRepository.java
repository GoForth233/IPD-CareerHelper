package com.group1.career.repository;

import com.group1.career.model.entity.InterviewQuestion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterviewQuestionRepository extends JpaRepository<InterviewQuestion, Long> {

    /**
     * Public list — supports filters that come straight from the /pages/market UI.
     * Both filters are optional ({@code null} treats as "any"). Sorting is
     * controlled by the caller's {@link Pageable}; the controller defaults to
     * "popular" (likes desc, createdAt desc).
     */
    @Query("select q from InterviewQuestion q where q.status = 'APPROVED' " +
            "and (:position is null or q.position = :position) " +
            "and (:difficulty is null or q.difficulty = :difficulty)")
    Page<InterviewQuestion> search(@Param("position") String position,
                                   @Param("difficulty") String difficulty,
                                   Pageable pageable);

    /**
     * Pull a small candidate pool for the random opening-question draw.
     * Returns approved questions matching position+difficulty, sorted by
     * draw_count asc so we slowly cover the long tail rather than picking
     * the same top-liked question every time. Limit is enforced by the
     * caller's {@link Pageable#getPageSize()}.
     */
    @Query("select q from InterviewQuestion q where q.status = 'APPROVED' " +
            "and q.position = :position and q.difficulty = :difficulty " +
            "order by q.drawCount asc, q.likes desc")
    List<InterviewQuestion> drawPool(@Param("position") String position,
                                     @Param("difficulty") String difficulty,
                                     Pageable limit);

    @Modifying
    @Query("update InterviewQuestion q set q.likes = q.likes + 1 where q.id = :id")
    int incrementLikes(@Param("id") Long id);

    @Modifying
    @Query("update InterviewQuestion q set q.drawCount = q.drawCount + 1 where q.id = :id")
    int incrementDrawCount(@Param("id") Long id);
}
