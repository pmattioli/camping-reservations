package com.pmattioli.campsite.reservations.service;

import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.util.AssertionErrors.assertNotEquals;
import static org.springframework.test.util.AssertionErrors.assertTrue;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import com.pmattioli.campsite.reservations.data.repo.Reservation;
import com.pmattioli.campsite.reservations.data.repo.ReservationRepository;
import com.pmattioli.campsite.reservations.data.repo.User;
import com.pmattioli.campsite.reservations.util.ReservationTestUtil;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {"campsite.reservation.length.maximum=3","campsite.reservation.list.default=3",
"campsite.reservation.days-ahead.minimum=1","campsite.reservation.days-ahead.maximum=30"})
public class ReservationsServiceTest {

    public static final Instant START_DATE_UTC = Instant.now().plus(3, ChronoUnit.DAYS);
    public static final Instant END_DATE_UTC = START_DATE_UTC.plus(3, ChronoUnit.DAYS);

    @Value("${campsite.reservation.list.default}")
    private int campsiteReservationLengthDefault;

    @Value("${campsite.reservation.length.maximum}")
    private int campsiteReservationLengthMaximum;

    @Value("${campsite.reservation.days-ahead.minimum}")
    private int campsiteReservationDaysAheadMinimum;

    @Value("${campsite.reservation.days-ahead.maximum}")
    private int campsiteReservationDaysAheadMaximum;

    @Autowired
    private ReservationsService reservationsService;

    @MockBean
    private ReservationRepository reservationRepository;

    @Rule
    public ExpectedException thrown = ExpectedException.none();


    @Test
    public void testCreateShouldThrowExceptionWhenConflict(){

        given(this.reservationRepository.findReservationsConflictingWithRange(START_DATE_UTC, END_DATE_UTC)).willReturn(
                Collections.singletonList(new Reservation()));

        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("An existing reservation conflicts with the selected dates");

        reservationsService.createReservation(null, START_DATE_UTC,
                START_DATE_UTC.plus(campsiteReservationLengthMaximum, ChronoUnit.DAYS));

    }

    @Test
    public void testCreateWhenDatesAvailableWithStartDateAndEndDateShouldBeSuccessful(){

        given(this.reservationRepository.findReservationsConflictingWithRange(START_DATE_UTC, END_DATE_UTC)).willReturn(
                Collections.emptyList());

        reservationsService.createReservation(null, START_DATE_UTC, START_DATE_UTC.plus(3, ChronoUnit.DAYS));

        verify(this.reservationRepository).findReservationsConflictingWithRange(START_DATE_UTC, END_DATE_UTC);

    }

    @Test
    public void testReservationRepositoryReturnsNull(){

        given(this.reservationRepository.findReservationsConflictingWithRange(START_DATE_UTC, END_DATE_UTC)).willReturn(
                null);

        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Illegal state: 'reservationsConflictingWithRange' list is null");

        reservationsService.createReservation(null, START_DATE_UTC, START_DATE_UTC.plus(3, ChronoUnit.DAYS));

    }

    @Test
    public void testCreateReservationSuccesfullyReturnsBookingID(){

        User user = ReservationTestUtil.createUser("Florencia", "Prieto", "florencia.prieto@disney.com");

        Reservation mockReservation = ReservationTestUtil.createReservation(START_DATE_UTC, END_DATE_UTC, user);
        mockReservation.setId(39491L);

        given(this.reservationRepository.save(any(Reservation.class)))
                .willReturn(mockReservation);

        Reservation reservation =
                reservationsService.createReservation(user, START_DATE_UTC, END_DATE_UTC);

        assertNotEquals("Reservation ID cannot be null", reservation.getId(), null);
        assertEquals("Wrong Reservation ID", reservation.getId(), 39491L);

    }

    @Test
    public void testReservationLengthExceedsMaximumLengthThrowsException(){

        Instant endDate = START_DATE_UTC.plus(campsiteReservationLengthMaximum + 1, ChronoUnit.DAYS);

        User user = ReservationTestUtil.createUser("Florencia", "Prieto", "florencia.prieto@disney.com");

        Reservation mockReservation = ReservationTestUtil.createReservation(START_DATE_UTC, endDate, user);
        mockReservation.setId(39491L);

        given(this.reservationRepository.save(any(Reservation.class)))
                .willReturn(mockReservation);

        thrown.expect(IllegalArgumentException.class);

        reservationsService.createReservation(user, START_DATE_UTC, endDate);

    }

    @Test
    public void testReservationLengthEqualsMaximumLengthIsSuccessful(){

        // given
        Instant startDate = START_DATE_UTC;
        Instant endDate = START_DATE_UTC.plus(campsiteReservationLengthMaximum, ChronoUnit.DAYS);

        User user = ReservationTestUtil.createUser("Florencia", "Prieto", "florencia.prieto@disney.com");

        Reservation mockReservation = ReservationTestUtil.createReservation(startDate, endDate, user);
        mockReservation.setId(39491L);

        given(this.reservationRepository.save(any(Reservation.class)))
                .willReturn(mockReservation);

        Reservation reservation =
                reservationsService.createReservation(user, startDate, endDate);

        assertNotEquals("Reservation ID cannot be null", reservation.getId(), null);
        assertEquals("Wrong Reservation ID", reservation.getId(), 39491L);

    }

    @Test
    public void testReservationStartingBeforeMinimumDaysAheadThrowsException(){

        // given
        Instant startDate = Instant.now().truncatedTo(ChronoUnit.DAYS)
                .plus(campsiteReservationDaysAheadMinimum, ChronoUnit.DAYS)
                .minus(1, ChronoUnit.DAYS);

        Instant endDate = startDate.plus(campsiteReservationLengthMaximum, ChronoUnit.DAYS);

        User user = ReservationTestUtil.createUser("Florencia", "Prieto", "florencia.prieto@disney.com");

        Reservation mockReservation = ReservationTestUtil.createReservation(startDate, endDate, user);
        mockReservation.setId(39491L);

        given(this.reservationRepository.save(any(Reservation.class)))
                .willReturn(mockReservation);

        thrown.expect(IllegalArgumentException.class);

        reservationsService.createReservation(user, startDate, endDate);

    }

    @Test
    public void testReservationStartingAfterMaximumDaysAheadThrowsException(){

        // given
        Instant startDate = Instant.now().plus(campsiteReservationDaysAheadMaximum, ChronoUnit.DAYS)
                .plus(1, ChronoUnit.DAYS);
        Instant endDate = startDate.plus(campsiteReservationLengthMaximum, ChronoUnit.DAYS);

        User user = ReservationTestUtil.createUser("Florencia", "Prieto", "florencia.prieto@disney.com");

        Reservation mockReservation = ReservationTestUtil.createReservation(startDate, endDate, user);
        mockReservation.setId(39491L);

        given(this.reservationRepository.save(any(Reservation.class)))
                .willReturn(mockReservation);

        thrown.expect(IllegalArgumentException.class);

        reservationsService.createReservation(user, startDate, endDate);

    }

    @Test
    public void testCreateReservationThrowsExceptionWhenDatesNotAvailable(){

        User user = ReservationTestUtil.createUser("Florencia", "Prieto", "florencia.prieto@disney.com");

        given(this.reservationRepository.findReservationsConflictingWithRange(START_DATE_UTC, END_DATE_UTC)).willReturn(
                Collections.singletonList(new Reservation()));

        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("An existing reservation conflicts with the selected dates");

        reservationsService.createReservation(user, START_DATE_UTC, END_DATE_UTC);

    }

    @Test
    public void testUpdateReservationSuccessfullyReturnsUpdatedReservation(){

        User user = ReservationTestUtil.createUser("Florencia", "Prieto", "florencia.prieto@disney.com");

        Reservation reservation = ReservationTestUtil.createReservation(START_DATE_UTC, END_DATE_UTC, user);
        reservation.setId(1L);
        reservation.setVersion(1L);

        given(this.reservationRepository.save(reservation)).willReturn(reservation);

        Reservation updatedReservation = reservationsService.updateReservation(reservation);

        assertEquals("Updated reservation ID has wrong value", updatedReservation.getId(), 1L);

    }

    @Test
    public void testUpdateReservationWithNullIdThrowsIllegalArgumentException(){

        User user = ReservationTestUtil.createUser("Florencia", "Prieto", "florencia.prieto@disney.com");

        Reservation reservation = ReservationTestUtil.createReservation(START_DATE_UTC, END_DATE_UTC, user);

        given(this.reservationRepository.save(reservation)).willReturn(reservation);

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Reservation ID cannot be null for UPDATE operations");

        reservationsService.updateReservation(reservation);

    }

    @Test
    public void testUpdateReservationWithNullVersionThrowsIllegalArgumentException(){

        User user = ReservationTestUtil.createUser("Florencia", "Prieto", "florencia.prieto@disney.com");

        Reservation reservation = ReservationTestUtil.createReservation(START_DATE_UTC, END_DATE_UTC, user);
        reservation.setId(1L);

        given(this.reservationRepository.save(reservation)).willReturn(reservation);

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Reservation version cannot be null for UPDATE operations");

        reservationsService.updateReservation(reservation);

    }

    @Test
    public void testDeleteReservationSuccessfully(){

        User user = ReservationTestUtil.createUser("Florencia", "Prieto", "florencia.prieto@disney.com");

        Reservation reservation = ReservationTestUtil.createReservation(START_DATE_UTC, END_DATE_UTC, user);
        reservation.setId(1L);
        reservation.setVersion(1L);

        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);

        reservationsService.deleteReservation(reservation);

        verify(this.reservationRepository).delete(captor.capture());

        assertEquals("Deleted reservation ID has wrong value", captor.getValue().getId(), 1L);

    }

    @Test
    public void testDeleteReservationWithNullIdThrowsIllegalArgumentException(){

        User user = ReservationTestUtil.createUser("Florencia", "Prieto", "florencia.prieto@disney.com");

        Reservation reservation = ReservationTestUtil.createReservation(START_DATE_UTC, END_DATE_UTC, user);

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Reservation ID cannot be null for DELETE operations");

        reservationsService.deleteReservation(reservation);

    }

    @Test
    public void testDeleteReservationWithNullVersionThrowsIllegalArgumentException(){

        User user = ReservationTestUtil.createUser("Florencia", "Prieto", "florencia.prieto@disney.com");

        Reservation reservation = ReservationTestUtil.createReservation(START_DATE_UTC, END_DATE_UTC, user);
        reservation.setId(1L);

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Reservation version cannot be null for DELETE operations");

        reservationsService.deleteReservation(reservation);

    }

    @SpringBootApplication
    static class TestConfiguration {
    }

}
