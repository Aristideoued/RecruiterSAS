package com.recruitersaas.repository;

import com.recruitersaas.model.RecruiterProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RecruiterProfileRepository extends JpaRepository<RecruiterProfile, String> {

    Optional<RecruiterProfile> findByUserId(String userId);

    Optional<RecruiterProfile> findBySlug(String slug);

    boolean existsBySlug(String slug);

    @Query("SELECT rp FROM RecruiterProfile rp JOIN rp.user u WHERE u.enabled = :enabled")
    Page<RecruiterProfile> findAllByUserEnabled(boolean enabled, Pageable pageable);

    @Query("SELECT rp FROM RecruiterProfile rp JOIN rp.user u WHERE u.email = :email")
    Optional<RecruiterProfile> findByUserEmail(String email);
}
