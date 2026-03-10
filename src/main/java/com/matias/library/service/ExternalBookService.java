package com.matias.library.service;

import com.matias.library.dto.external.GoogleBooksResponseDTO;
import com.matias.library.exception.BadRequestException;
import com.matias.library.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class ExternalBookService {

    private final RestClient restClient;
    @Value("${google.books.api-key}")
    private String apiKey;
    private static final String GOOGLE_BOOKS_API_URL = "https://www.googleapis.com/books/v1/volumes?q=isbn:";

    public GoogleBooksResponseDTO.VolumeInfo fetchBookByIsbn(String isbn) {
        try {

            String url = GOOGLE_BOOKS_API_URL + isbn + "&key=" + apiKey;

            GoogleBooksResponseDTO response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(GoogleBooksResponseDTO.class);

            if (response == null || response.getItems() == null || response.getItems().isEmpty()) {
                throw new NotFoundException("Book not found in external API for ISBN: " + isbn);
            }

            return response.getItems().getFirst().getVolumeInfo();

        } catch (HttpClientErrorException e) {
            throw new BadRequestException("Error querying external API: " + e.getMessage());
        }
    }
}