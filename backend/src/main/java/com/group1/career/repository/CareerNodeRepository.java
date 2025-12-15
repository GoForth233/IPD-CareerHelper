package com.group1.career.repository;

import com.group1.career.model.entity.CareerNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CareerNodeRepository extends JpaRepository<CareerNode, Long> {
    List<CareerNode> findByPathIdOrderByLevelAsc(Integer pathId);
}

