package com.group1.career.repository;

import com.group1.career.model.entity.HomeVideo;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HomeVideoRepository extends JpaRepository<HomeVideo, Long> {

    Optional<HomeVideo> findByBvid(String bvid);

    /**
     * Pseudo-random sampling. We use MySQL's {@code RAND(seed)} so the same
     * user gets a stable batch within a single day (we pass dayOfYear+userId
     * as the seed) but a fresh slice the next morning.
     */
    @Query(value = "SELECT * FROM home_videos ORDER BY RAND(:seed) LIMIT :limit",
           nativeQuery = true)
    List<HomeVideo> sampleByRand(@Param("seed") long seed, @Param("limit") int limit);

    /** Fallback used when MySQL's RAND() sampling isn't available (H2 tests). */
    List<HomeVideo> findAllByOrderBySortScoreDesc(Pageable pageable);

    @Modifying
    @Transactional
    @Query("delete from HomeVideo v where v.fetchedAt < :cutoff")
    int deleteOlderThan(@Param("cutoff") LocalDateTime cutoff);
}
