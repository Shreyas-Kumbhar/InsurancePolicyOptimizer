package com.suraksha.shield.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "policies")
public class Policy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @NotBlank
    @Column(nullable = false)
    private String type;

    @NotNull
    @Min(0)
    @Column(nullable = false)
    private Integer premium;

    @NotNull
    @Min(0)
    @Column(nullable = false)
    private Integer coverage;

    @NotBlank
    @Column(name = "risk_level", nullable = false)
    private String riskLevel;

    @NotBlank
    @Column(nullable = false)
    private String provider;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by_id")
    @JsonIgnore
    private Admin createdBy;
}
