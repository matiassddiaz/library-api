package com.matias.library.service;

import com.matias.library.dto.GenreRequestDTO;
import com.matias.library.dto.GenreResponseDTO;
import com.matias.library.dto.PaginatedResponseDTO;
import com.matias.library.exception.ConflictException;
import com.matias.library.exception.NotFoundException;
import com.matias.library.mapper.EntityMapper;
import com.matias.library.model.Genre;
import com.matias.library.repository.GenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class GenreService implements IGenreService{

    private final GenreRepository repository;
    private final EntityMapper entityMapper;

    @Override
    @Transactional
    public GenreResponseDTO createGenre(GenreRequestDTO dto) {
        if (repository.existsByName(dto.getName())) {
            throw new ConflictException("Genre name already exists: " + dto.getName());
        }
        Genre genre = entityMapper.toEntity(dto);
        Genre savedGenre = repository.save(genre);
        return entityMapper.toDTO(savedGenre);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponseDTO<GenreResponseDTO> getAllGenres(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Genre> genrePage = repository.findAll(pageable);
        List<GenreResponseDTO> listOfGenres = genrePage.getContent().stream()
                .map(entityMapper::toDTO)
                .collect(Collectors.toList());

        return PaginatedResponseDTO.<GenreResponseDTO>builder()
                .content(listOfGenres)
                .pageNo(genrePage.getNumber())
                .pageSize(genrePage.getSize())
                .totalElements(genrePage.getTotalElements())
                .totalPages(genrePage.getTotalPages())
                .last(genrePage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public GenreResponseDTO getGenreById(Long id) {
        Genre genre = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("No genre was found using ID:" + id));
        return entityMapper.toDTO(genre);

    }

    @Override
    @Transactional
    public GenreResponseDTO updateGenre(Long id, GenreRequestDTO dto) {
        Genre genre = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("No genre was found using ID: " + id));
        if (repository.existsByNameAndIdNot(dto.getName(), id)) {
            throw new ConflictException("Genre name already exists: " + dto.getName());
        }

        genre.setName(dto.getName());

        Genre updatedGenre = repository.save(genre);
        return entityMapper.toDTO(updatedGenre);
    }

    @Override
    @Transactional
    public void deleteGenre(Long id) {
        Genre genre = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("No genre was found using ID: " + id));
        repository.delete(genre);
    }
}
