package com.matias.library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GenreRequestDTO {
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    @NotBlank(message = "Name is mandatory and cannot be blank")
    private String name;
}
