package com.matias.library.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ImportBookRequestDTO {

    @NotBlank(message = "ISBN is mandatory")
    private String isbn;

    @NotNull(message = "Stock is mandatory")
    @Min(value = 1, message = "Stock must be at least 1 when importing a new book")
    private Integer stock;

    @NotNull(message = "Library ID is mandatory")
    private Long libraryId;

    @NotNull(message = "Genre IDs cannot be null")
    @Size(min = 1, message = "The book must have at least one associated genre")
    private Set<Long> genreIds;
}