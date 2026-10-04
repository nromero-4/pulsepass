package com.pulsepass.pulsepass.mapper;

import com.pulsepass.pulsepass.domain.Venue;
import com.pulsepass.pulsepass.dto.response.VenueResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface VenueMapper {
    @Mapping(target = "id", source = "id")
    VenueResponse toResponse(Venue venue);
}
