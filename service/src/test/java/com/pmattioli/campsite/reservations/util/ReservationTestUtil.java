package com.pmattioli.campsite.reservations.util;

import java.time.Instant;

import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.pmattioli.campsite.reservations.data.repo.Reservation;
import com.pmattioli.campsite.reservations.data.repo.User;

public class ReservationTestUtil {


    public static User createUser(final String firstName, final String lastName, final String email) {
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);

        return user;
    }

    public static Reservation createReservation(final Instant startDate, final Instant endDate) {
        Reservation reservation = new Reservation();
        reservation.setStartDate(startDate);
        reservation.setEndDate(endDate);

        return reservation;
    }

    public static Reservation createReservation(final Instant startDate, final Instant endDate, final User user) {
        Reservation reservation = createReservation(startDate, endDate);
        reservation.setUser(user);

        return reservation;
    }

    public static Reservation copy(Reservation originalReservation) {
        Reservation staleReservation = new Reservation(originalReservation.getStartDate(),
                originalReservation.getEndDate(), originalReservation.getUser());
        staleReservation.setId(originalReservation.getId());
        staleReservation.setVersion(originalReservation.getVersion());
        return staleReservation;
    }
}
