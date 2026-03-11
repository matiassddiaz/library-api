package com.matias.library.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookResponseDTO {
    private Long id;
    private String isbn;
    private String title;
    private String author;
    private String synopsis;
    private String publishedDate;
    private String imageUrl;
    private Integer stock;
    private String libraryName;
    private Set<String> genreNames;
}