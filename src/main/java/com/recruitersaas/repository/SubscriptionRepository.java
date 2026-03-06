package com.recruitersaas.repository;

import com.recruitersaas.model.Subscription;
import com.recruitersaas.model.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, String> {

    Optional<Subscription> findByRecruiterProfileId(String recruiterProfileId);

    List<Subscription> findAllByStatus(SubscriptionStatus status);

    boolean existsByRecruiterProfileId(String recruiterProfileId);
}
