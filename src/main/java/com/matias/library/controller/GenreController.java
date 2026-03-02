package com.matias.library.controller;

import com.matias.library.dto.GenreRequestDTO;
import com.matias.library.dto.GenreResponseDTO;
import com.matias.library.dto.PaginatedResponseDTO;
import com.matias.library.service.IGenreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/genres")
@RequiredArgsConstructor
public class GenreController {

    private final IGenreService genreService;

    @PostMapping
    public ResponseEntity<GenreResponseDTO> createGenre(@Valid @RequestBody GenreRequestDTO dto){
        GenreResponseDTO createdGenre = genreService.createGenre(dto);
        URI location = URI.create("/api/genres/" + createdGenre.getId());
        return ResponseEntity.created(location).body(createdGenre);
    }

    @GetMapping
    public ResponseEntity<PaginatedResponseDTO<GenreResponseDTO>> getAllGenres(
            @RequestParam(value = "page", defaultValue = "0", required = false) int page,
            @RequestParam(value = "size", defaultValue = "10", required = false) int size){
        return ResponseEntity.ok(genreService.getAllGenres(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GenreResponseDTO> getGenreById(@PathVariable Long id){
        GenreResponseDTO genre = genreService.getGenreById(id);
        return ResponseEntity.ok(genre);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GenreResponseDTO> updateGenre (@PathVariable Long id,
                                                         @Valid @RequestBody GenreRequestDTO dto){
        return ResponseEntity.ok(genreService.updateGenre(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGenre (@PathVariable Long id){
        genreService.deleteGenre(id);
        return ResponseEntity.noContent().build();
    }

}
