package com.suraksha.shield.repository;

import com.suraksha.shield.entity.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, Long> {

    @Query(value = """
            SELECT *
            FROM policies
            WHERE name ILIKE '%' || :name || '%'
            ORDER BY coverage / NULLIF(premium, 0) DESC
            LIMIT 100
            """, nativeQuery = true)
    List<Policy> findByName(@Param("name") String name);

    @Query(value = """
            SELECT *
            FROM policies
            WHERE type = :type
              AND premium <= :maxPremium
            ORDER BY coverage / NULLIF(premium, 0) DESC
            LIMIT 200
            """, nativeQuery = true)
    List<Policy> findByType(
            @Param("type") String type,
            @Param("maxPremium") Integer maxPremium);

    @Query(value = """
            SELECT *
            FROM policies
            WHERE risk_level = :riskLevel
              AND premium <= :maxPremium
            ORDER BY coverage / NULLIF(premium, 0) DESC
            LIMIT 200
            """, nativeQuery = true)
    List<Policy> findByRiskLevel(
            @Param("riskLevel") String riskLevel,
            @Param("maxPremium") Integer maxPremium);

    @Query(value = """
            SELECT *
            FROM policies
            WHERE type = :type
              AND risk_level = :riskLevel
              AND premium <= :maxPremium
            ORDER BY coverage / NULLIF(premium, 0) DESC
            LIMIT 200
            """, nativeQuery = true)
    List<Policy> findByTypeAndRiskLevel(
            @Param("type") String type,
            @Param("riskLevel") String riskLevel,
            @Param("maxPremium") Integer maxPremium);

    @Query(value = """
            SELECT *
            FROM policies
            WHERE premium <= :maxPremium
            ORDER BY coverage / NULLIF(premium, 0) DESC
            LIMIT 200
            """, nativeQuery = true)
    List<Policy> findMostEfficient(
            @Param("maxPremium") Integer maxPremium);

    @Query(value = """
            SELECT *
            FROM policies
            ORDER BY id DESC
            LIMIT 100
            """, nativeQuery = true)
    List<Policy> findLatestPolicies();
}
