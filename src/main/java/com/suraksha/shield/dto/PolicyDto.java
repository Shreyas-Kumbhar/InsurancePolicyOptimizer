package com.suraksha.shield.dto;

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
public class PolicyDto {

    private Long id;

    @NotBlank(message = "Policy name is required")
    private String name;

    @NotBlank(message = "Policy type is required")
    private String type;

    @NotNull(message = "Premium is required")
    @Min(value = 0, message = "Premium must be non-negative")
    private Integer premium;

    @NotNull(message = "Coverage is required")
    @Min(value = 0, message = "Coverage must be non-negative")
    private Integer coverage;

    @NotBlank(message = "Risk level is required")
    private String riskLevel;

    @NotBlank(message = "Provider is required")
    private String provider;

    private Long createdById;
}
