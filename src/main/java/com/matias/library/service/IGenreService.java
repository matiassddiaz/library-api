package com.matias.library.service;

import com.matias.library.dto.GenreRequestDTO;
import com.matias.library.dto.GenreResponseDTO;
import com.matias.library.dto.PaginatedResponseDTO;

public interface IGenreService {
    GenreResponseDTO createGenre(GenreRequestDTO dto);
    PaginatedResponseDTO<GenreResponseDTO> getAllGenres(int page, int size);
    GenreResponseDTO getGenreById(Long id);
    GenreResponseDTO updateGenre(Long id, GenreRequestDTO dto);
    void deleteGenre(Long id);
}
