package com.pulsepass.pulsepass.service.impl;

import com.pulsepass.pulsepass.dto.response.VenueResponse;
import com.pulsepass.pulsepass.service.VenueService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class VenueServiceImpl implements VenueService {
    @Override
    public VenueResponse findByCode(String code) {
        throw new UnsupportedOperationException("VenueServiceImpl.findByCode not implemented yet");
    }

    @Override
    public List<VenueResponse> findActiveVenues() {
        throw new UnsupportedOperationException("VenueServiceImpl.findActiveVenues not implemented yet");
    }
}
