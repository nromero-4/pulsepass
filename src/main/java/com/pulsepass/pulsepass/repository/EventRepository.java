package com.pulsepass.pulsepass.repository;

import com.pulsepass.pulsepass.domain.Event;
import com.pulsepass.pulsepass.enums.EventStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventRepository extends JpaRepository<Event, Long> {

    Optional<Event> findByEventCode(String eventCode);

    List<Event> findByStatusOrderByEventDateAsc(EventStatus status);

    List<Event> findByVenueCode(String venueCode);

    @Query("SELECT e FROM Event e JOIN e.artists a WHERE a.stageName = :stageName")
    List<Event> findByArtistStageName(@Param("stageName") String stageName);

    @Query("SELECT DISTINCT e FROM Event e JOIN e.venue v JOIN e.artists a " +
           "WHERE v.city = :city AND a.stageName = :stageName")
    List<Event> findByCityAndArtist(@Param("city") String city, @Param("stageName") String stageName);

    @Query("SELECT DISTINCT e FROM Event e JOIN e.venue v JOIN e.artists a " +
           "WHERE e.status = :status AND e.eventDate >= :date AND v.city = :city AND lower(a.stageName) LIKE lower(concat('%', :artistText, '%')) " +
           "ORDER BY e.eventDate ASC")
    List<Event> findRecommendedEvents(@Param("status") EventStatus status,
                                      @Param("date") LocalDate date,
                                      @Param("city") String city,
                                      @Param("artistText") String artistText);
}
