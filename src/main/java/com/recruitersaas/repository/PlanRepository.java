package com.recruitersaas.repository;

import com.recruitersaas.model.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlanRepository extends JpaRepository<Plan, String> {

    List<Plan> findAllByActiveTrue();

    Optional<Plan> findByName(String name);

    boolean existsByName(String name);
}
