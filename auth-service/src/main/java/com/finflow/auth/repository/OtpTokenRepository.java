package com.finflow.auth.repository;

import com.finflow.auth.model.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {

    Optional<OtpToken> findTopByEmailAndUsedAtIsNullOrderByCreatedAtDesc(String email);

    List<OtpToken> findAllByEmailAndUsedAtIsNull(String email);

    @Modifying
    @Transactional
    void deleteAllByEmailAndUsedAtIsNull(String email);
}









