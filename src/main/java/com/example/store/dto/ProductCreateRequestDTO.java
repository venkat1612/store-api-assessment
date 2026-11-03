package com.example.store.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Data;

@Data
public class ProductCreateRequestDTO {
    @NotBlank
    @Size(max = 255)
    private String description;
}
