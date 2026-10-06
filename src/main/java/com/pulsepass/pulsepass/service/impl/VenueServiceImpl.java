package com.pulsepass.pulsepass.service.impl;

import com.pulsepass.pulsepass.domain.Venue;
import com.pulsepass.pulsepass.dto.response.VenueResponse;
import com.pulsepass.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.pulsepass.mapper.VenueMapper;
import com.pulsepass.pulsepass.repository.VenueRepository;
import com.pulsepass.pulsepass.service.VenueService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class VenueServiceImpl implements VenueService {
    private final VenueRepository venueRepository;
    private final VenueMapper venueMapper;

    public VenueServiceImpl(VenueRepository venueRepository, VenueMapper venueMapper) {
        this.venueRepository = venueRepository;
        this.venueMapper = venueMapper;
    }

    @Override
    public VenueResponse findByCode(String code) {
        Venue venue = venueRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found: " + code));
        return venueMapper.toResponse(venue);
    }

    @Override
    public List<VenueResponse> findActiveVenues() {
        return venueRepository.findByActiveTrueOrderByNameAsc().stream()
                .map(venueMapper::toResponse)
                .toList();
    }
}
