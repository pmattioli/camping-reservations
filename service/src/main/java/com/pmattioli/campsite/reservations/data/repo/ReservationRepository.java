package com.pmattioli.campsite.reservations.data.repo;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("SELECT r FROM Reservation r WHERE (r.startDate < :endDate AND r.endDate > :startDate) "
            + "OR (r.endDate < :startDate AND r.startDate > :endDate)")
    List<Reservation> findReservationsConflictingWithRange(Instant startDate, Instant endDate);

}
