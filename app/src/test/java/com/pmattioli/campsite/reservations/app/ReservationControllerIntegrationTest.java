package com.pmattioli.campsite.reservations.app;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import javax.transaction.Transactional;

import org.apache.logging.log4j.util.Strings;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pmattioli.campsite.reservations.controller.exception.ExceptionConverter;
import com.pmattioli.campsite.reservations.controller.model.ReservationJson;
import com.pmattioli.campsite.reservations.data.repo.Reservation;
import com.pmattioli.campsite.reservations.data.repo.User;
import com.pmattioli.campsite.reservations.util.ReservationTestUtil;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@AutoConfigureTestEntityManager
public class ReservationControllerIntegrationTest {

    public static final Instant TODAY = Instant.now().truncatedTo(ChronoUnit.DAYS);
    public static final Instant END_DATE_UTC = TODAY.plus(2, ChronoUnit.DAYS);

    @Value("${campsite.reservation.length.maximum}")
    private int campsiteReservationLengthMaximum;

    @Value("${campsite.reservation.days-ahead.minimum}")
    private int campsiteReservationDaysAheadMinimum;

    @Value("${campsite.reservation.list.default}")
    private int campsiteReservationListReservationsDefault;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ModelMapper modelMapper;

    private final ExceptionConverter exConverter = new ExceptionConverter(this);

    @Test
    @Transactional
    public void givenOnlyStartDate_whenReservationsExist_thenStatus200AndNonEmptyJson()
            throws Exception {

        User user = ReservationTestUtil.createUser("Florencia", "Prieto", "florpri@gmail.com");
        Instant firstRsvStartDate = TODAY.plus(campsiteReservationDaysAheadMinimum, ChronoUnit.DAYS);
        Instant firstRsvEndDate = firstRsvStartDate.plus(campsiteReservationLengthMaximum, ChronoUnit.DAYS);

        createReservation(firstRsvStartDate, firstRsvEndDate, user);

        // Create a second reservation which starts X days after today, where X is the default number of days
        // configured for the listReservations endpoint. This is in order to test (by verifying that this second
        // reservation is returned) that when no end date is passed to the listReservations endpoint, the
        // default is used

        Instant secondRsvStartDate = TODAY.plus(campsiteReservationListReservationsDefault, ChronoUnit.DAYS);
        Instant secondRsvEndDate = secondRsvStartDate.plus(campsiteReservationLengthMaximum, ChronoUnit.DAYS);

        createReservation(firstRsvEndDate, secondRsvEndDate, user);

        // List reservations should return the previously created reservations

        mvc.perform(get("/v1/reservations?startDate=" + firstRsvStartDate)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("[0].booking_id", notNullValue()))
                .andExpect(jsonPath("[1].booking_id", notNullValue()));
    }

    @Test
    @Transactional
    public void givenOnlyStartDate_whenNoReservation_thenStatus200AndEmptyJson()
            throws Exception {

        User user = ReservationTestUtil.createUser("Florencia", "Prieto", "florpri@gmail.com");
        Instant firstRsvStartDate = TODAY.plus(campsiteReservationDaysAheadMinimum, ChronoUnit.DAYS);
        Instant firstRsvEndDate = firstRsvStartDate.plus(campsiteReservationLengthMaximum, ChronoUnit.DAYS);

        createReservation(firstRsvStartDate, firstRsvEndDate, user);

        mvc.perform(get("/v1/reservations?startDate=" + firstRsvEndDate)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));
    }

    @Test
    @Transactional
    public void givenDates_whenNoReservation_thenStatus200AndEmptyJson()
            throws Exception {

        User user = ReservationTestUtil.createUser("Florencia", "Prieto", "florpri@gmail.com");
        Instant firstRsvStartDate = TODAY.plus(campsiteReservationDaysAheadMinimum, ChronoUnit.DAYS);
        Instant firstRsvEndDate = firstRsvStartDate.plus(campsiteReservationLengthMaximum, ChronoUnit.DAYS);

        createReservation(firstRsvStartDate, firstRsvEndDate, user);

        Instant secondRsvStartDate = firstRsvStartDate.plus(campsiteReservationLengthMaximum * 2, ChronoUnit.DAYS);
        Instant secondRsvEndDate = secondRsvStartDate.plus(campsiteReservationLengthMaximum, ChronoUnit.DAYS);

        createReservation(secondRsvStartDate, secondRsvEndDate, user);

        // Attempt to create a reservation in the exact dates between the previous two should be successful

        mvc.perform(get("/v1/reservations?startDate=" + firstRsvEndDate +
                "&numberOfDays=" + campsiteReservationLengthMaximum)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));
    }

    @Test
    @Transactional
    public void givenDates_whenAreDatesNotAvailable_thenStatus409()
            throws Exception {

        User user = ReservationTestUtil.createUser("Florencia", "Prieto", "florpri@gmail.com");

        Instant firstRsvStartDate = TODAY.plus(campsiteReservationDaysAheadMinimum, ChronoUnit.DAYS);
        Instant firstRsvEndDate = firstRsvStartDate.plus(campsiteReservationLengthMaximum, ChronoUnit.DAYS);

        createReservation(firstRsvStartDate, firstRsvEndDate, user);

        int secondRsvLength = this.campsiteReservationLengthMaximum;
        Instant secondRsvStartDate = firstRsvStartDate.plus(9, ChronoUnit.DAYS);
        Instant secondRsvEndDate = secondRsvStartDate.plus(secondRsvLength, ChronoUnit.DAYS);

        createReservation(secondRsvStartDate, secondRsvEndDate, user);

        // Attempt to make a third reservation which starts before the second one has ended
        // (campsiteReservationLengthMaximum - 1). Specifically, it starts secondRsvLength - 1 days
        // before secondRsvEndDate, so they overlap

        Instant thirdRsvStartDate = secondRsvEndDate.minus(secondRsvLength - 1, ChronoUnit.DAYS);

        Reservation thirdRsv = ReservationTestUtil.createReservation(thirdRsvStartDate,
                thirdRsvStartDate.plus(campsiteReservationLengthMaximum, ChronoUnit.DAYS), user);

        ReservationJson thirdRsvJson = modelMapper.map(thirdRsv, ReservationJson.class);

        mvc.perform(post("/v1/reservations")
                .content(objectMapper.writeValueAsString(thirdRsvJson))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(content().json(exConverter.toBuilder(new IllegalArgumentException(
                        "An existing reservation conflicts with the selected dates")).errorCode("409")
                        .wrap().toString()));
    }

    @Test
    public void givenAReservation_whenSuccesful_thenStatus200AndReturnBookingId()
            throws Exception {

        User user = ReservationTestUtil.createUser("Florencia", "Prieto", "florpri@gmail.com");

        Instant startDate = TODAY.plus(campsiteReservationDaysAheadMinimum, ChronoUnit.DAYS);
        Instant endDate = startDate.plus(campsiteReservationLengthMaximum, ChronoUnit.DAYS);

        Reservation reservation = ReservationTestUtil.createReservation(startDate, endDate, user);

        ReservationJson reservationJson = modelMapper.map(reservation, ReservationJson.class);

        mvc.perform(post("/v1/reservations").content(objectMapper.writeValueAsString(reservationJson))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("booking_id", notNullValue()));
    }

    @Test
    @Transactional
    public void givenAReservation_whenUpdateSuccesful_thenStatus200AndReturnUpdatedReservation()
            throws Exception {

        User user = ReservationTestUtil.createUser("Florencia", "Prieto", "florpri@gmail.com");
        Reservation reservation =
                createReservation(TODAY, END_DATE_UTC, user);

        ReservationJson reservationJson = modelMapper.map(reservation, ReservationJson.class);

        reservationJson.setEndDate(END_DATE_UTC.plus(2, ChronoUnit.DAYS));

        mvc.perform(put("/v1/reservations").content(objectMapper.writeValueAsString(reservationJson))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("end_date", is(END_DATE_UTC.plus(2, ChronoUnit.DAYS).toString())));
    }

    @Test
    @Transactional
    public void givenAReservation_whenDeleteSuccesful_thenStatus204AndDatesAreAvailable()
            throws Exception {

        User user = ReservationTestUtil.createUser("Florencia", "Prieto", "florpri@gmail.com");

        // Create first reservation

        Instant conflictingStartDate = TODAY.plus(9, ChronoUnit.DAYS);
        Instant conflictingEndDate = conflictingStartDate.plus(campsiteReservationLengthMaximum, ChronoUnit.DAYS);

        Reservation conflictsWithEndDateReservation = createReservation(conflictingStartDate, conflictingEndDate, user);

        ReservationJson reservationJson = modelMapper.map(conflictsWithEndDateReservation, ReservationJson.class);

        // Create second reservation which should conflict with the first one

        Reservation secondRsv = ReservationTestUtil.createReservation(conflictingStartDate.minus(1, ChronoUnit.DAYS),
                conflictingEndDate.minus(1, ChronoUnit.DAYS), user);

        ReservationJson secondRsvJson = modelMapper.map(secondRsv, ReservationJson.class);

        mvc.perform(post("/v1/reservations").content(objectMapper.writeValueAsString(secondRsvJson))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(content().json(exConverter.toBuilder(new IllegalArgumentException(
                        "An existing reservation conflicts with the selected dates")).errorCode("409")
                        .wrap().toString()));

        // Delete first reservation

        mvc.perform(delete("/v1/reservations").content(objectMapper.writeValueAsString(reservationJson))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andExpect(content().string(Strings.EMPTY));

        // And now a new attempt to create the second reservation should be successful

        mvc.perform(post("/v1/reservations").content(objectMapper.writeValueAsString(secondRsvJson))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("booking_id", notNullValue()));
    }

    @Test
    @Transactional
    public void givenTwoVesionsOfReservation_whenConcurrentUpdate_thenStatus409AndErrorMessage()
            throws Exception {

        User user = ReservationTestUtil.createUser("Florencia", "Prieto", "florpri@gmail.com");
        Reservation reservation =
                createReservation(TODAY, END_DATE_UTC, user);

        ReservationJson reservationJson = modelMapper.map(reservation, ReservationJson.class);

        reservationJson.setEndDate(END_DATE_UTC.plus(2, ChronoUnit.DAYS));

        mvc.perform(put("/v1/reservations").content(objectMapper.writeValueAsString(reservationJson))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("end_date", is(END_DATE_UTC.plus(2, ChronoUnit.DAYS).toString())));

        reservationJson.setEndDate(END_DATE_UTC.plus(4, ChronoUnit.DAYS));

        entityManager.flush();

        mvc.perform(put("/v1/reservations").content(objectMapper.writeValueAsString(reservationJson))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("errors[0].error_code", is("409")));
    }

    private Reservation createReservation(Instant startDate, Instant endDate, User user) {
        Reservation reservation = ReservationTestUtil.createReservation(startDate, endDate, user);
        return entityManager.merge(reservation);
    }

}
