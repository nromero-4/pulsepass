package com.pulsepass.pulsepass.mapper;

import com.pulsepass.pulsepass.domain.Artist;
import com.pulsepass.pulsepass.domain.Event;
import com.pulsepass.pulsepass.dto.response.EventResponse;
import com.pulsepass.pulsepass.dto.response.EventSummaryResponse;
import java.util.List;
import java.util.Set;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EventMapper {
    @Mapping(target = "venueCode", source = "venue.code")
    @Mapping(target = "venueName", source = "venue.name")
    @Mapping(target = "artists", expression = "java(mapArtists(event.getArtists()))")
    EventResponse toResponse(Event event);

    @Mapping(target = "venueCode", source = "venue.code")
    @Mapping(target = "venueName", source = "venue.name")
    @Mapping(target = "artists", expression = "java(mapArtists(event.getArtists()))")
    EventSummaryResponse toSummary(Event event);

    default List<String> mapArtists(Set<Artist> artists) {
        if (artists == null || artists.isEmpty()) {
            return List.of();
        }
        return artists.stream()
                .map(Artist::getStageName)
                .sorted()
                .toList();
    }
}
