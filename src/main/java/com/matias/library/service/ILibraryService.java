package com.matias.library.service;

import com.matias.library.dto.LibraryRequestDTO;
import com.matias.library.dto.LibraryResponseDTO;
import com.matias.library.dto.PaginatedResponseDTO;

public interface ILibraryService {
    LibraryResponseDTO createLibrary(LibraryRequestDTO dto);
    PaginatedResponseDTO<LibraryResponseDTO> getAllLibraries(int page, int size);
    LibraryResponseDTO getLibraryById(Long id);
    LibraryResponseDTO updateLibrary(Long id, LibraryRequestDTO dto);
    void deleteLibrary(Long id);
}
