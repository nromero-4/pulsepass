package com.pulsepass.pulsepass.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pulsepass.pulsepass.domain.Venue;
import com.pulsepass.pulsepass.dto.response.VenueResponse;
import com.pulsepass.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.pulsepass.mapper.VenueMapper;
import com.pulsepass.pulsepass.repository.VenueRepository;
import com.pulsepass.pulsepass.service.impl.VenueServiceImpl;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VenueServiceImplTest {

    @Mock
    private VenueRepository venueRepository;

    @Mock
    private VenueMapper venueMapper;

    @InjectMocks
    private VenueServiceImpl venueService;

    @Test
    void findByCodeReturnsMappedVenue() {
        Venue venue = venue();
        VenueResponse response = response();
        when(venueRepository.findByCode("VEN-1")).thenReturn(Optional.of(venue));
        when(venueMapper.toResponse(venue)).thenReturn(response);

        VenueResponse result = venueService.findByCode("VEN-1");

        assertThat(result).isSameAs(response);
        verify(venueMapper).toResponse(venue);
    }

    @Test
    void findByCodeThrowsWhenVenueDoesNotExist() {
        when(venueRepository.findByCode("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> venueService.findByCode("missing"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("missing");
    }

    @Test
    void findActiveVenuesReturnsMappedActiveVenues() {
        Venue venue = venue();
        VenueResponse response = response();
        when(venueRepository.findByActiveTrueOrderByNameAsc()).thenReturn(List.of(venue));
        when(venueMapper.toResponse(venue)).thenReturn(response);

        List<VenueResponse> result = venueService.findActiveVenues();

        assertThat(result).containsExactly(response);
        verify(venueRepository).findByActiveTrueOrderByNameAsc();
    }

    private Venue venue() {
        return new Venue("VEN-1", "Marina Center", "Santa Marta", "Calle 1", 500);
    }

    private VenueResponse response() {
        return new VenueResponse(1L, "VEN-1", "Marina Center", "Santa Marta", "Calle 1", 500, true);
    }
}
