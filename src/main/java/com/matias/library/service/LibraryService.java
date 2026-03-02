package com.matias.library.service;

import com.matias.library.dto.LibraryRequestDTO;
import com.matias.library.dto.LibraryResponseDTO;
import com.matias.library.dto.PaginatedResponseDTO;
import com.matias.library.exception.ConflictException;
import com.matias.library.exception.NotFoundException;
import com.matias.library.mapper.EntityMapper;
import com.matias.library.model.Library;
import com.matias.library.repository.LibraryRepository;
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
public class LibraryService implements ILibraryService{

    private final LibraryRepository repository;
    private final EntityMapper entityMapper;

    @Override
    @Transactional
    public LibraryResponseDTO createLibrary(LibraryRequestDTO dto) {
        if (repository.existsByNameAndAddress(dto.getName(), dto.getAddress())) {
            throw new ConflictException("A library already exists with name " + dto.getName() + " at address "+ dto.getAddress());
        }
        Library library =  entityMapper.toEntity(dto);
        Library savedLibrary = repository.save(library);
        return entityMapper.toDTO(savedLibrary);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponseDTO<LibraryResponseDTO> getAllLibraries(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Library> libraryPage = repository.findAll(pageable);
        List<LibraryResponseDTO> listOfLibraries = libraryPage.getContent().stream()
                .map(entityMapper::toDTO)
                .collect(Collectors.toList());
        return PaginatedResponseDTO.<LibraryResponseDTO>builder()
                .content(listOfLibraries)
                .pageNo(libraryPage.getNumber())
                .pageSize(libraryPage.getSize())
                .totalElements(libraryPage.getTotalElements())
                .totalPages(libraryPage.getTotalPages())
                .last(libraryPage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public LibraryResponseDTO getLibraryById(Long id) {
        Library library = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("No library was found using ID:" + id));
        return entityMapper.toDTO(library);
    }

    @Override
    @Transactional
    public LibraryResponseDTO updateLibrary(Long id, LibraryRequestDTO dto) {
        Library library = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("No library was found using ID:" + id));
        if (repository.existsByNameAndAddressAndIdNot(dto.getName(), dto.getAddress(), id)) {
            throw new ConflictException("A library already exists with name " + dto.getName() + " at address "+ dto.getAddress());
        }
        library.setAddress(dto.getAddress());
        library.setName(dto.getName());
        Library savedLibrary = repository.save(library);
        return entityMapper.toDTO(savedLibrary);
    }

    @Override
    @Transactional
    public void deleteLibrary(Long id) {
        Library library = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("No library was found using ID:" + id));
        repository.delete(library);
    }
}
