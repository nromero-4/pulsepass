package com.pulsepass.pulsepass.service.impl;

import com.pulsepass.pulsepass.dto.request.PurchaseTicketRequest;
import com.pulsepass.pulsepass.dto.response.TicketResponse;
import com.pulsepass.pulsepass.service.TicketService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class TicketServiceImpl implements TicketService {
    @Override
    public TicketResponse purchase(PurchaseTicketRequest request) {
        throw new UnsupportedOperationException("TicketServiceImpl.purchase not implemented yet");
    }

    @Override
    public TicketResponse findByCode(String ticketCode) {
        throw new UnsupportedOperationException("TicketServiceImpl.findByCode not implemented yet");
    }

    @Override
    public List<TicketResponse> findByUserEmail(String email) {
        throw new UnsupportedOperationException("TicketServiceImpl.findByUserEmail not implemented yet");
    }

    @Override
    public List<TicketResponse> findPaidTicketsByEvent(String eventCode) {
        throw new UnsupportedOperationException("TicketServiceImpl.findPaidTicketsByEvent not implemented yet");
    }

    @Override
    public TicketResponse cancel(String ticketCode) {
        throw new UnsupportedOperationException("TicketServiceImpl.cancel not implemented yet");
    }

    @Override
    public TicketResponse markAsUsed(String ticketCode) {
        throw new UnsupportedOperationException("TicketServiceImpl.markAsUsed not implemented yet");
    }
}
