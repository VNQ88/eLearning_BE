package org.example.elearningbe.user.verification_code;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Integer> {
    Optional<VerificationCode> findByCode(String code);
    Optional<VerificationCode> findByUserId(Long userId);
    void deleteByCode(String code);
    void deleteByUserId(Long userId);
}
