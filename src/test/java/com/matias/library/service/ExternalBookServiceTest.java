package com.matias.library.service;

import com.matias.library.dto.external.GoogleBooksResponseDTO;
import com.matias.library.exception.BadRequestException;
import com.matias.library.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExternalBookServiceTest {

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestHeadersUriSpec<?> requestHeadersUriSpec;

    @Mock
    private RestClient.RequestHeadersSpec<?> requestHeadersSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @InjectMocks
    private ExternalBookService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "apiKey", "test-api-key");
    }

    private void mockRestClientChain() {
        doReturn(requestHeadersUriSpec).when(restClient).get();
        doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(anyString());
        doReturn(responseSpec).when(requestHeadersSpec).retrieve();
    }

    // ─── fetchBookByIsbn ──────────────────────────────────────────────────────

    @Test
    void fetchBookByIsbn_WhenIsbnExists_ShouldReturnVolumeInfo() {
        GoogleBooksResponseDTO.VolumeInfo volumeInfo = new GoogleBooksResponseDTO.VolumeInfo();
        volumeInfo.setTitle("1984");
        volumeInfo.setAuthors(List.of("George Orwell"));

        GoogleBooksResponseDTO.Item item = new GoogleBooksResponseDTO.Item();
        item.setVolumeInfo(volumeInfo);

        GoogleBooksResponseDTO responseDTO = new GoogleBooksResponseDTO();
        responseDTO.setItems(List.of(item));

        mockRestClientChain();
        when(responseSpec.body(GoogleBooksResponseDTO.class)).thenReturn(responseDTO);

        GoogleBooksResponseDTO.VolumeInfo result = service.fetchBookByIsbn("9780451524935");

        assertNotNull(result);
        assertEquals("1984", result.getTitle());
        assertEquals(List.of("George Orwell"), result.getAuthors());
    }

    @Test
    void fetchBookByIsbn_WhenApiReturnsNull_ShouldThrowNotFoundException() {
        mockRestClientChain();
        when(responseSpec.body(GoogleBooksResponseDTO.class)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> service.fetchBookByIsbn("0000000000"));
    }

    @Test
    void fetchBookByIsbn_WhenItemsListIsNull_ShouldThrowNotFoundException() {
        GoogleBooksResponseDTO responseDTO = new GoogleBooksResponseDTO();
        responseDTO.setItems(null);

        mockRestClientChain();
        when(responseSpec.body(GoogleBooksResponseDTO.class)).thenReturn(responseDTO);

        assertThrows(NotFoundException.class, () -> service.fetchBookByIsbn("0000000000"));
    }

    @Test
    void fetchBookByIsbn_WhenItemsListIsEmpty_ShouldThrowNotFoundException() {
        GoogleBooksResponseDTO responseDTO = new GoogleBooksResponseDTO();
        responseDTO.setItems(List.of());

        mockRestClientChain();
        when(responseSpec.body(GoogleBooksResponseDTO.class)).thenReturn(responseDTO);

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> service.fetchBookByIsbn("0000000000"));
        assertTrue(ex.getMessage().contains("0000000000"));
    }

    @Test
    void fetchBookByIsbn_WhenApiReturns4xx_ShouldThrowBadRequestException() {
        mockRestClientChain();
        when(responseSpec.body(GoogleBooksResponseDTO.class))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad Request"));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> service.fetchBookByIsbn("invalid-isbn"));
        assertTrue(ex.getMessage().contains("Error querying external API"));
    }

    @Test
    void fetchBookByIsbn_WhenApiReturns403_ShouldThrowBadRequestException() {
        mockRestClientChain();
        when(responseSpec.body(GoogleBooksResponseDTO.class))
                .thenThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN, "API key invalid"));

        assertThrows(BadRequestException.class, () -> service.fetchBookByIsbn("9780451524935"));
    }
}