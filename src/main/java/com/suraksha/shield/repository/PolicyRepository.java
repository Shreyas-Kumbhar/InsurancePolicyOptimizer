package com.suraksha.shield.repository;

import com.suraksha.shield.entity.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, Long> {
    @org.springframework.data.jpa.repository.Query(value = "SELECT * FROM policies WHERE name LIKE %:name% ORDER BY (coverage / premium) DESC LIMIT 100", nativeQuery = true)
    List<Policy> findByNameContainingIgnoreCase(@org.springframework.data.repository.query.Param("name") String name);

    @org.springframework.data.jpa.repository.Query(value = "SELECT * FROM policies WHERE type = :type AND premium <= :maxPremium ORDER BY (coverage / premium) DESC LIMIT 200", nativeQuery = true)
    List<Policy> findTop200ByType(@org.springframework.data.repository.query.Param("type") String type, @org.springframework.data.repository.query.Param("maxPremium") Integer maxPremium);

    @org.springframework.data.jpa.repository.Query(value = "SELECT * FROM policies WHERE risk_level = :riskLevel AND premium <= :maxPremium ORDER BY (coverage / premium) DESC LIMIT 200", nativeQuery = true)
    List<Policy> findTop200ByRiskLevel(@org.springframework.data.repository.query.Param("riskLevel") String riskLevel, @org.springframework.data.repository.query.Param("maxPremium") Integer maxPremium);

    @org.springframework.data.jpa.repository.Query(value = "SELECT * FROM policies WHERE type = :type AND risk_level = :riskLevel AND premium <= :maxPremium ORDER BY (coverage / premium) DESC LIMIT 200", nativeQuery = true)
    List<Policy> findTop200ByTypeAndRiskLevel(@org.springframework.data.repository.query.Param("type") String type, @org.springframework.data.repository.query.Param("riskLevel") String riskLevel, @org.springframework.data.repository.query.Param("maxPremium") Integer maxPremium);

    @org.springframework.data.jpa.repository.Query(value = "SELECT * FROM policies WHERE premium <= :maxPremium ORDER BY (coverage / premium) DESC LIMIT 200", nativeQuery = true)
    List<Policy> findTop200MostEfficient(@org.springframework.data.repository.query.Param("maxPremium") Integer maxPremium);
    
    @org.springframework.data.jpa.repository.Query(value = "SELECT * FROM policies ORDER BY id DESC LIMIT 100", nativeQuery = true)
    List<Policy> findTop100Policies();
}
