package com.suraksha.shield.repository;

import com.suraksha.shield.entity.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, Long> {
    List<Policy> findByNameContainingIgnoreCase(String name);
    List<Policy> findByTypeIgnoreCase(String type);
    List<Policy> findByRiskLevelIgnoreCase(String riskLevel);
    List<Policy> findByTypeIgnoreCaseAndRiskLevelIgnoreCase(String type, String riskLevel);
}
