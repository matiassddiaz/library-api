package com.matias.library.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class LibraryRequestDTO {
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    @NotBlank(message = "Name is mandatory and cannot be blank")
    private String name;
    @Size(max = 255, message = "Name cannot exceed 255 characters")
    @NotBlank(message = "Address is mandatory and cannot be blank")
    private String address;
}
