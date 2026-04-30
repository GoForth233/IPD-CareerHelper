package com.group1.career.repository;

import com.group1.career.model.entity.HomeConsultation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HomeConsultationRepository extends JpaRepository<HomeConsultation, Long> {

    List<HomeConsultation> findAllByOrderByPublishedAtDescIdDesc(Pageable pageable);
}
