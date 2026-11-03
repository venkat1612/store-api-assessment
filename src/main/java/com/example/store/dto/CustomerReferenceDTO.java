package com.example.store.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import lombok.Data;

@Data
public class CustomerReferenceDTO {
    @NotNull @Positive private Long id;
}
