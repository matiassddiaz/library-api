package com.matias.library.controller;

import com.matias.library.dto.LibraryRequestDTO;
import com.matias.library.dto.LibraryResponseDTO;
import com.matias.library.dto.PaginatedResponseDTO;
import com.matias.library.service.ILibraryService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/libraries")
@RequiredArgsConstructor
public class LibraryController {

    private final ILibraryService libraryService;

    @PostMapping
    @ApiResponse(responseCode = "201", description = "Library successfully created")
    public ResponseEntity<LibraryResponseDTO> createLibrary(@Valid @RequestBody LibraryRequestDTO dto){
        LibraryResponseDTO createdLibrary = libraryService.createLibrary(dto);
        URI location = URI.create("/api/libraries/" + createdLibrary.getId());
        return ResponseEntity.created(location).body(createdLibrary);
    }

    @GetMapping
    public ResponseEntity<PaginatedResponseDTO<LibraryResponseDTO>> getAllLibraries(
            @RequestParam(value = "page", defaultValue = "0", required = false) int page,
            @RequestParam(value = "size", defaultValue = "10", required = false) int size){
        return ResponseEntity.ok(libraryService.getAllLibraries(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LibraryResponseDTO> getLibraryById(@PathVariable Long id){
        LibraryResponseDTO library = libraryService.getLibraryById(id);
        return ResponseEntity.ok(library);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LibraryResponseDTO> updateLibrary(@PathVariable Long id,
                                                      @Valid @RequestBody LibraryRequestDTO dto){
        return ResponseEntity.ok(libraryService.updateLibrary(id, dto));
    }

    @DeleteMapping("/{id}")
    @ApiResponse(responseCode = "204", description = "Library successfully deleted")
    public ResponseEntity<Void> deleteLibrary(@PathVariable Long id){
        libraryService.deleteLibrary(id);
        return ResponseEntity.noContent().build();
    }
}
