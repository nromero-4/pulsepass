package com.pulsepass.pulsepass.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pulsepass.pulsepass.domain.Artist;
import com.pulsepass.pulsepass.dto.response.ArtistResponse;
import com.pulsepass.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.pulsepass.mapper.ArtistMapper;
import com.pulsepass.pulsepass.repository.ArtistRepository;
import com.pulsepass.pulsepass.service.impl.ArtistServiceImpl;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ArtistServiceImplTest {

    @Mock
    private ArtistRepository artistRepository;

    @Mock
    private ArtistMapper artistMapper;

    @InjectMocks
    private ArtistServiceImpl artistService;

    @Test
    void findByIdReturnsMappedArtist() {
        Artist artist = artist();
        ArtistResponse response = response();
        when(artistRepository.findById(1L)).thenReturn(Optional.of(artist));
        when(artistMapper.toResponse(artist)).thenReturn(response);

        ArtistResponse result = artistService.findById(1L);

        assertThat(result).isSameAs(response);
        verify(artistMapper).toResponse(artist);
    }

    @Test
    void findByIdThrowsWhenArtistDoesNotExist() {
        when(artistRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> artistService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void findByStageNameReturnsMappedArtistUsingIgnoreCaseLookup() {
        Artist artist = artist();
        ArtistResponse response = response();
        when(artistRepository.findByStageNameIgnoreCase("SOLAR BEAT")).thenReturn(Optional.of(artist));
        when(artistMapper.toResponse(artist)).thenReturn(response);

        ArtistResponse result = artistService.findByStageName("SOLAR BEAT");

        assertThat(result).isSameAs(response);
        verify(artistRepository).findByStageNameIgnoreCase("SOLAR BEAT");
    }

    @Test
    void findByStageNameThrowsWhenArtistDoesNotExist() {
        when(artistRepository.findByStageNameIgnoreCase("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> artistService.findByStageName("unknown"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("unknown");
    }

    @Test
    void findActiveArtistsReturnsMappedActiveArtists() {
        Artist artist = artist();
        ArtistResponse response = response();
        when(artistRepository.findByActiveTrueOrderByStageNameAsc()).thenReturn(List.of(artist));
        when(artistMapper.toResponse(artist)).thenReturn(response);

        List<ArtistResponse> result = artistService.findActiveArtists();

        assertThat(result).containsExactly(response);
        verify(artistRepository).findByActiveTrueOrderByStageNameAsc();
    }

    private Artist artist() {
        return new Artist("Solar Beat", "Colombia", "Electronic");
    }

    private ArtistResponse response() {
        return new ArtistResponse(1L, "Solar Beat", "Colombia", "Electronic", true);
    }
}
