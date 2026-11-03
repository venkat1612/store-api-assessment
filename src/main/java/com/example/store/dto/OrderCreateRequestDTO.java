package com.example.store.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class OrderCreateRequestDTO {
    @NotBlank
    @Size(max = 255)
    private String description;

    @NotNull @Valid
    private CustomerReferenceDTO customer;

    @NotEmpty
    private List<@NotNull @Positive Long> productIds;
}
