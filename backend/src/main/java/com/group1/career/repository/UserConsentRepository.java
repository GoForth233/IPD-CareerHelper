package com.group1.career.repository;

import com.group1.career.model.entity.UserConsent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserConsentRepository extends JpaRepository<UserConsent, Long> {
    Optional<UserConsent> findByUserIdAndAgreementVersion(Long userId, String agreementVersion);
    boolean existsByUserIdAndAgreementVersion(Long userId, String agreementVersion);
}
