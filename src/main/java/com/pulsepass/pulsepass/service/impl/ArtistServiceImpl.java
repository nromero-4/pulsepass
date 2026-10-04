package com.pulsepass.pulsepass.service.impl;

import com.pulsepass.pulsepass.dto.response.ArtistResponse;
import com.pulsepass.pulsepass.service.ArtistService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ArtistServiceImpl implements ArtistService {
    @Override
    public ArtistResponse findById(Long id) {
        throw new UnsupportedOperationException("ArtistServiceImpl.findById not implemented yet");
    }

    @Override
    public ArtistResponse findByStageName(String stageName) {
        throw new UnsupportedOperationException("ArtistServiceImpl.findByStageName not implemented yet");
    }

    @Override
    public List<ArtistResponse> findActiveArtists() {
        throw new UnsupportedOperationException("ArtistServiceImpl.findActiveArtists not implemented yet");
    }
}
