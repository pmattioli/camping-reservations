package com.pmattioli.campsite.reservations.data.repo;

import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.util.AssertionErrors.assertNotEquals;
import static org.springframework.test.util.AssertionErrors.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.junit4.SpringRunner;

import com.pmattioli.campsite.reservations.util.ReservationTestUtil;

@RunWith(SpringRunner.class)
@DataJpaTest
public class ReservationRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ReservationRepository reservationRepository;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testFindReservationsBetweenTwoDatesForOverlappingRange(){

        // given
        createReservation(Instant.parse("2018-05-12T00:00:00Z"), Instant.parse("2018-05-17T00:00:00Z"));

        List<Reservation> conflictingReservations = reservationRepository.findReservationsConflictingWithRange(
                Instant.parse("2018-05-12T00:00:00Z"), Instant.parse("2018-05-17T00:00:00Z"));

        assertTrue("No reservation found between specified dates", !conflictingReservations.isEmpty());

    }

    @Test
    public void testFindReservationsBetweenTwoDatesForStartDateOverlappingRange(){

        // given
        createReservation(Instant.parse("2018-05-10T00:00:00Z"), Instant.parse("2018-05-15T00:00:00Z"));

        List<Reservation> conflictingReservations = reservationRepository.findReservationsConflictingWithRange(
                Instant.parse("2018-05-12T00:00:00Z"), Instant.parse("2018-05-17T00:00:00Z"));

        assertTrue("No reservation found between specified dates", !conflictingReservations.isEmpty());

    }

    @Test
    public void testFindReservationsBetweenTwoDatesForEndDateOverlappingRange(){

        // given
        createReservation(Instant.parse("2018-05-14T00:00:00Z"), Instant.parse("2018-05-19T00:00:00Z"));

        List<Reservation> conflictingReservations = reservationRepository.findReservationsConflictingWithRange(
                Instant.parse("2018-05-12T00:00:00Z"), Instant.parse("2018-05-17T00:00:00Z"));

        assertTrue("No reservation found between specified dates", !conflictingReservations.isEmpty());

    }

    @Test
    public void testFindReservationsBetweenTwoDatesForNonOverlappingRange(){

        // given
        createReservation(Instant.parse("2018-05-18T00:00:00Z"), Instant.parse("2018-05-20T00:00:00Z"));

        List<Reservation> conflictingReservations = reservationRepository.findReservationsConflictingWithRange(
                Instant.parse("2018-05-12T00:00:00Z"), Instant.parse("2018-05-17T00:00:00Z"));

        assertTrue("There should be no reservation between specified dates", conflictingReservations.isEmpty());

    }

    @Test
    public void testFindReservationsBetweenTwoDatesForBackToBackStartDateNonOverlappingRange(){

        // given
        createReservation(Instant.parse("2018-05-18T00:00:00Z"), Instant.parse("2018-05-20T00:00:00Z"));

        List<Reservation> conflictingReservations = reservationRepository.findReservationsConflictingWithRange(
                Instant.parse("2018-05-12T00:00:00Z"), Instant.parse("2018-05-18T00:00:00Z"));

        assertTrue("There should be no reservation between specified dates", conflictingReservations.isEmpty());

    }

    @Test
    public void testFindReservationsBetweenTwoDatesForBackToBackEndDateNonOverlappingRange(){

        // given
        createReservation(Instant.parse("2018-05-18T00:00:00Z"), Instant.parse("2018-05-20T00:00:00Z"));

        List<Reservation> conflictingReservations = reservationRepository.findReservationsConflictingWithRange(
                Instant.parse("2018-05-20T00:00:00Z"), Instant.parse("2018-05-22T00:00:00Z"));

        assertTrue("There should be no reservation between specified dates", conflictingReservations.isEmpty());

    }

    @Test
    public void testCreateReservationSuccesfullyReturnsBookingID() {

        User user = ReservationTestUtil.createUser("Florencia", "Prieto", "florencia.prieto@disney.com");

        Reservation newReservation = ReservationTestUtil.createReservation(Instant.parse("2018-01-01T00:00:00Z"),
                Instant.parse("2018-01-10T00:00:00Z"), user);

        Reservation reservation = reservationRepository.save(newReservation);

        assertNotEquals("Reservation ID should not be null", null, reservation.getId());

    }

    @Test
    public void testCreateReservationSuccesfullyWithExistingUserReturnsBookingID() {

        Reservation newReservation = new Reservation();
        newReservation.setStartDate(Instant.parse("2018-01-01T00:00:00Z"));
        newReservation.setEndDate(Instant.parse("2018-01-10T00:00:00Z"));

        User user = createUser("Florencia", "Prieto", "florencia.prieto@disney.com");
        newReservation.setUser(user);

        Reservation persistedReservation = reservationRepository.save(newReservation);

        assertNotEquals("Reservation ID should not be null", null, persistedReservation.getId());

    }

    @Test
    public void testUpdateReservationSuccessfullyReturnsUpdatedReservation() {

        User user = createUser("Florencia", "Prieto", "florencia.prieto@disney.com");

        Reservation existingReservation =
                createReservation(Instant.parse("2018-05-18T00:00:00Z"), Instant.parse("2018-05-20T00:00:00Z"), user);

        assertEquals("End date for existing reservation differs from expected",
                existingReservation.getEndDate(), Instant.parse("2018-05-20T00:00:00Z"));

        existingReservation.setEndDate(Instant.parse("2018-05-22T00:00:00Z"));

        Reservation updatedReservation = reservationRepository.save(existingReservation);

        assertNotEquals("Reservation ID should not be null", null, updatedReservation.getId());

        assertEquals("Existing and updated IDs should be equal",
                updatedReservation.getId(), existingReservation.getId());
        assertEquals("End date for updated reservation differs from expected",
                updatedReservation.getEndDate(), Instant.parse("2018-05-22T00:00:00Z"));

    }

    @Test
    public void testOptimisticLockingPreventsSilentUpdates() {

        User user = createUser("Florencia", "Prieto", "florencia.prieto@disney.com");

        Reservation newReservation = ReservationTestUtil.createReservation(Instant.parse("2018-05-18T00:00:00Z"),
                        Instant.parse("2018-05-20T00:00:00Z"), user);

        reservationRepository.save(newReservation);

        Reservation staleReservation = ReservationTestUtil.copy(newReservation);

        staleReservation.setEndDate(Instant.parse("2018-05-22T00:00:00Z"));

        newReservation.setEndDate(Instant.parse("2018-05-24T00:00:00Z"));

        // This one should succeed
        reservationRepository.saveAndFlush(newReservation);

        thrown.expect(ObjectOptimisticLockingFailureException.class);

        // This one should fail (copy is stale)
        reservationRepository.save(staleReservation);

    }

    @Test
    public void testDeleteReservationSuccessfully() {

        User user = createUser("Florencia", "Prieto", "florencia.prieto@disney.com");

        Reservation existingReservation =
                createReservation(Instant.parse("2018-05-18T00:00:00Z"), Instant.parse("2018-05-20T00:00:00Z"), user);

        Optional<Reservation> persistedReservation = reservationRepository.findById(existingReservation.getId());

        assertTrue("Reservation should have been found", persistedReservation.isPresent());

        reservationRepository.delete(existingReservation);

        Optional<Reservation> deletedReservation = reservationRepository.findById(existingReservation.getId());

        assertTrue("Reservation should have been deleted", !deletedReservation.isPresent());

    }

    @Test
    public void testOptimisticLockingPreventsSilentDelete() {

        User user = createUser("Florencia", "Prieto", "florencia.prieto@disney.com");

        Reservation newReservation = ReservationTestUtil.createReservation(Instant.parse("2018-05-18T00:00:00Z"),
                Instant.parse("2018-05-20T00:00:00Z"), user);

        reservationRepository.save(newReservation);

        Reservation staleReservation = ReservationTestUtil.copy(newReservation);

        staleReservation.setEndDate(Instant.parse("2018-05-22T00:00:00Z"));

        newReservation.setEndDate(Instant.parse("2018-05-24T00:00:00Z"));

        // The update operation should succeed
        reservationRepository.saveAndFlush(newReservation);

        thrown.expect(ObjectOptimisticLockingFailureException.class);

        // This delete operation should fail (copy is stale)
        reservationRepository.delete(staleReservation);

    }

    private User createUser(String firstName, String lastName, String email) {
        User user = ReservationTestUtil.createUser(firstName, lastName, email);
        return entityManager.persist(user);
    }

    private Reservation createReservation(Instant startDate, Instant endDate) {
        Reservation reservation = ReservationTestUtil.createReservation(startDate, endDate);
        return entityManager.persist(reservation);
    }

    private Reservation createReservation(Instant startDate, Instant endDate, User user) {
        Reservation reservation = ReservationTestUtil.createReservation(startDate, endDate, user);
        return entityManager.persist(reservation);
    }

    @SpringBootApplication
    @EnableJpaRepositories
    static class TestConfiguration {
    }

}
