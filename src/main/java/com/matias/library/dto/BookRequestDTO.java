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
public class BookRequestDTO {

    @NotBlank(message = "ISBN is mandatory")
    private String isbn;

    @Size(max = 255, message = "Title cannot exceed 255 characters")
    @NotBlank(message = "Title is mandatory and cannot be blank")
    private String title;

    @Size(max = 100, message = "Author's name cannot exceed 100 characters")
    @NotBlank(message = "Author is mandatory")
    private String author;

    private String synopsis;
    private String publishedDate;
    private String imageUrl;

    @NotNull(message = "Stock is mandatory")
    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stock;

    @NotNull(message = "Library ID is mandatory")
    private Long libraryId;

    @NotNull(message = "Genre IDs cannot be null")
    @Size(min = 1, message = "The book must have at least one associated genre")
    private Set<Long> genreIds;
}