package com.matias.library.mapper;

import com.matias.library.dto.*;
import com.matias.library.model.Book;
import com.matias.library.model.Genre;
import com.matias.library.model.Library;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class EntityMapper {

    public BookResponseDTO toDTO(Book book){
        if (book == null) return null;
        String libraryName = (book.getLibrary() != null) ? book.getLibrary().getName() : "Unassigned";

        Set<String> genreNames = book.getGenres().stream()
                .map(Genre::getName)
                .collect(Collectors.toSet());
        return BookResponseDTO.builder()
                .id(book.getId())
                .isbn(book.getIsbn())
                .title(book.getTitle())
                .author(book.getAuthor())
                .synopsis(book.getSynopsis())
                .publishedDate(book.getPublishedDate())
                .imageUrl(book.getImageUrl())
                .stock(book.getStock())
                .libraryName(libraryName)
                .genreNames(genreNames)
                .build();
    }

    public Book toEntity(BookRequestDTO dto, Library library, Set<Genre> genres){
        return Book.builder()
                .isbn(dto.getIsbn())
                .title(dto.getTitle())
                .author(dto.getAuthor())
                .synopsis(dto.getSynopsis())
                .publishedDate(dto.getPublishedDate())
                .imageUrl(dto.getImageUrl())
                .stock(dto.getStock())
                .library(library)
                .genres(genres)
                .build();
    }

    public LibraryResponseDTO toDTO(Library library){
        if (library == null) return null;

        return LibraryResponseDTO.builder()
                .id(library.getId())
                .name(library.getName())
                .address(library.getAddress())
                .build();
    }

    public Library toEntity(LibraryRequestDTO dto){
        return Library.builder()
                .name(dto.getName())
                .address(dto.getAddress())
                .build();
    }

    public GenreResponseDTO toDTO(Genre genre){
        if (genre == null) return null;
        return GenreResponseDTO.builder()
                .id(genre.getId())
                .name(genre.getName())
                .build();
    }

    public Genre toEntity(GenreRequestDTO dto){
        return Genre.builder()
                .name(dto.getName())
                .build();
    }
}
