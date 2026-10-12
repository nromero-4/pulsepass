package com.pulsepass.pulsepass.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import tools.jackson.databind.ObjectMapper;
import com.pulsepass.pulsepass.dto.request.PurchaseTicketRequest;
import com.pulsepass.pulsepass.dto.response.TicketResponse;
import com.pulsepass.pulsepass.enums.TicketStatus;
import com.pulsepass.pulsepass.enums.TicketType;
import com.pulsepass.pulsepass.exception.BusinessRuleException;
import com.pulsepass.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.pulsepass.service.TicketService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TicketController.class)
class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TicketService ticketService;

    @Test
    void purchaseReturnsCreatedTicket() throws Exception {
        PurchaseTicketRequest request = validRequest();
        when(ticketService.purchase(request)).thenReturn(ticket(TicketStatus.PAID));

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.ticketCode").value("TCK-1"))
                .andExpect(jsonPath("$.status").value("PAID"));

        verify(ticketService).purchase(request);
    }

    @Test
    void purchaseRejectsInvalidRequestWithoutCallingService() throws Exception {
        PurchaseTicketRequest request = new PurchaseTicketRequest("invalid-email", "", null);

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details.userEmail").exists())
                .andExpect(jsonPath("$.details.eventCode").exists())
                .andExpect(jsonPath("$.details.type").exists());

        verify(ticketService, never()).purchase(any(PurchaseTicketRequest.class));
    }

    @Test
    void purchaseReturnsNotFoundWhenUserDoesNotExist() throws Exception {
        PurchaseTicketRequest request = validRequest();
        when(ticketService.purchase(request))
                .thenThrow(new ResourceNotFoundException("User not found: andrea@example.com"));

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("User not found: andrea@example.com"));

        verify(ticketService).purchase(request);
    }

    @Test
    void purchaseReturnsConflictWhenBusinessRuleFails() throws Exception {
        PurchaseTicketRequest request = validRequest();
        when(ticketService.purchase(request))
                .thenThrow(new BusinessRuleException("Tickets can only be purchased for published events"));

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Tickets can only be purchased for published events"));

        verify(ticketService).purchase(request);
    }

    @Test
    void findByCodeReturnsTicket() throws Exception {
        when(ticketService.findByCode("TCK-1")).thenReturn(ticket(TicketStatus.PAID));

        mockMvc.perform(get("/api/tickets/{ticketCode}", "TCK-1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.ticketCode").value("TCK-1"))
                .andExpect(jsonPath("$.status").value("PAID"));

        verify(ticketService).findByCode("TCK-1");
    }

    @Test
    void findByUserEmailReturnsTickets() throws Exception {
        when(ticketService.findByUserEmail("andrea@example.com"))
                .thenReturn(List.of(ticket(TicketStatus.PAID)));

        mockMvc.perform(get("/api/tickets/by-user").param("email", "andrea@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].ticketCode").value("TCK-1"));

        verify(ticketService).findByUserEmail("andrea@example.com");
    }

    @Test
    void findPaidTicketsByEventReturnsTickets() throws Exception {
        when(ticketService.findPaidTicketsByEvent("CMF-2026"))
                .thenReturn(List.of(ticket(TicketStatus.PAID)));

        mockMvc.perform(get("/api/events/{eventCode}/tickets/paid", "CMF-2026"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].eventCode").value("CMF-2026"))
                .andExpect(jsonPath("$[0].status").value("PAID"));

        verify(ticketService).findPaidTicketsByEvent("CMF-2026");
    }

    @Test
    void cancelReturnsUpdatedTicket() throws Exception {
        when(ticketService.cancel("TCK-1")).thenReturn(ticket(TicketStatus.CANCELLED));

        mockMvc.perform(patch("/api/tickets/{ticketCode}/cancel", "TCK-1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        verify(ticketService).cancel("TCK-1");
    }

    @Test
    void cancelReturnsConflictWhenTransitionIsRejected() throws Exception {
        when(ticketService.cancel("TCK-1"))
                .thenThrow(new BusinessRuleException("Only paid tickets can be cancelled"));

        mockMvc.perform(patch("/api/tickets/{ticketCode}/cancel", "TCK-1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Only paid tickets can be cancelled"));

        verify(ticketService).cancel("TCK-1");
    }

    @Test
    void markAsUsedReturnsUpdatedTicket() throws Exception {
        when(ticketService.markAsUsed("TCK-1")).thenReturn(ticket(TicketStatus.USED));

        mockMvc.perform(patch("/api/tickets/{ticketCode}/use", "TCK-1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("USED"));

        verify(ticketService).markAsUsed("TCK-1");
    }

    @Test
    void markAsUsedReturnsConflictWhenTransitionIsRejected() throws Exception {
        when(ticketService.markAsUsed("TCK-1"))
                .thenThrow(new BusinessRuleException("Only paid tickets can be marked as used"));

        mockMvc.perform(patch("/api/tickets/{ticketCode}/use", "TCK-1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Only paid tickets can be marked as used"));

        verify(ticketService).markAsUsed("TCK-1");
    }

    private PurchaseTicketRequest validRequest() {
        return new PurchaseTicketRequest("andrea@example.com", "CMF-2026", TicketType.GENERAL);
    }

    private TicketResponse ticket(TicketStatus status) {
        return new TicketResponse(1L, "TCK-1", TicketType.GENERAL, new BigDecimal("100000.00"), status,
                LocalDateTime.of(2026, 10, 1, 12, 0), "andrea@example.com",
                "CMF-2026", "Caribbean Music Fest 2026");
    }
}