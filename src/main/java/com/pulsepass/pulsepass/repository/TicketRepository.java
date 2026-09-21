package com.pulsepass.pulsepass.repository;

import com.pulsepass.pulsepass.domain.Ticket;
import com.pulsepass.pulsepass.enums.TicketStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByTicketCode(String ticketCode);

    List<Ticket> findByUserEmailIgnoreCase(String email);

    List<Ticket> findByUserEmailIgnoreCaseAndStatus(String email, TicketStatus status);

    @Query("SELECT t FROM Ticket t JOIN t.event e WHERE e.eventCode = :eventCode AND t.status = :status")
    List<Ticket> findByEventCodeAndStatus(@Param("eventCode") String eventCode, @Param("status") TicketStatus status);

    @Query("SELECT COUNT(t) FROM Ticket t JOIN t.event e WHERE e.eventCode = :eventCode AND t.status = :status")
    Long countByEventCodeAndStatus(@Param("eventCode") String eventCode, @Param("status") TicketStatus status);
}
