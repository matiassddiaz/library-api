package com.matias.library.repository;

import com.matias.library.model.Library;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LibraryRepository extends JpaRepository <Library, Long> {
    boolean existsByNameAndAddress(String name, String address);
    boolean existsByNameAndAddressAndIdNot(String name, String address, Long id);
}
