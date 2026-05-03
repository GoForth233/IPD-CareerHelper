package com.group1.career.repository;

import com.group1.career.model.entity.HomeArticle;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HomeArticleRepository extends JpaRepository<HomeArticle, Long> {

    /**
     * Newest-first by published date, falling back to row id so seeded rows
     * without a published_at still have a stable order.
     */
    List<HomeArticle> findAllByOrderByPublishedAtDescIdDesc(Pageable pageable);

    List<HomeArticle> findAllByHiddenFalseOrderByPinnedDescPublishedAtDescIdDesc(Pageable pageable);
}
