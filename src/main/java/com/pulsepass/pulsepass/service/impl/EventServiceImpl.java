package com.pulsepass.pulsepass.service.impl;

import com.pulsepass.pulsepass.dto.request.CreateEventRequest;
import com.pulsepass.pulsepass.dto.response.EventResponse;
import com.pulsepass.pulsepass.dto.response.EventSummaryResponse;
import com.pulsepass.pulsepass.service.EventService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class EventServiceImpl implements EventService {
    @Override
    public EventResponse create(CreateEventRequest request) {
        throw new UnsupportedOperationException("EventServiceImpl.create not implemented yet");
    }

    @Override
    public EventResponse findByCode(String eventCode) {
        throw new UnsupportedOperationException("EventServiceImpl.findByCode not implemented yet");
    }

    @Override
    public List<EventSummaryResponse> findPublishedEvents() {
        throw new UnsupportedOperationException("EventServiceImpl.findPublishedEvents not implemented yet");
    }

    @Override
    public EventResponse publish(String eventCode) {
        throw new UnsupportedOperationException("EventServiceImpl.publish not implemented yet");
    }

    @Override
    public EventResponse addArtist(String eventCode, Long artistId) {
        throw new UnsupportedOperationException("EventServiceImpl.addArtist not implemented yet");
    }

    @Override
    public List<EventSummaryResponse> findByArtist(String stageName) {
        throw new UnsupportedOperationException("EventServiceImpl.findByArtist not implemented yet");
    }
}
