package com.pmattioli.campsite.reservations.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pmattioli.campsite.reservations.controller.exception.ExceptionConverter;
import com.pmattioli.campsite.reservations.controller.model.ReservationJson;
import com.pmattioli.campsite.reservations.data.repo.Reservation;
import com.pmattioli.campsite.reservations.data.repo.User;
import com.pmattioli.campsite.reservations.service.ReservationsService;
import com.pmattioli.campsite.reservations.util.ReservationTestUtil;

@RunWith(SpringRunner.class)
@WebMvcTest(ReservationController.class)
public class ReservationControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ReservationsService reservationsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ModelMapper modelMapper;

    private final ExceptionConverter exConverter = new ExceptionConverter(this);

    @Test
    public void testListReservationsWithinTimeRangeReturns200WhenDatesAvailable() throws Exception {

        User testUser = ReservationTestUtil.createUser("Florencia", "Prieto", "florpri@gmail.com");
        Reservation testReservation = ReservationTestUtil.createReservation(Instant.parse("2018-09-22T00:00:00Z"),
                Instant.parse("2018-09-23T00:00:00Z"), testUser);

        ReservationJson request = modelMapper.map(testReservation, ReservationJson.class);

        testReservation.setId(1L);
        ReservationJson expectedResponse = modelMapper.map(testReservation, ReservationJson.class);
        given(this.reservationsService.listReservationsWithinTimeRange(request.getStartDate(), 10))
                .willReturn(Arrays.asList(testReservation));

        this.mvc.perform(get("/v1/reservations?startDate=2018-09-22T00:00:00Z&numberOfDays=10")
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(content()
                .json(objectMapper.writeValueAsString(Arrays.asList(expectedResponse))));

        verify(this.reservationsService).listReservationsWithinTimeRange(request.getStartDate(), 10);
    }

    @Test
    public void testListReservationsWithinTimeRangeReturns409WhenDatesAreNotAvailable() throws Exception {
        doThrow(new IllegalStateException("Illegal state")).when(this.reservationsService)
                .listReservationsWithinTimeRange(Instant.parse("2018-09-22T00:00:00Z"), 10);

        this.mvc.perform(get("/v1/reservations?startDate=2018-09-22T00:00:00Z&numberOfDays=10")
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isConflict()).andExpect(content()
                .json(exConverter.toBuilder(new IllegalArgumentException(
                        "Illegal state")).errorCode("409").wrap().toString()));

        verify(this.reservationsService).listReservationsWithinTimeRange(Instant.parse("2018-09-22T00:00:00Z"), 10);
    }

    @Test
    public void testNumberOfDaysIsEmptyShouldPassNullAndReturn200WhenDatesAreAvailable() throws Exception {

        this.mvc.perform(get("/v1/reservations?startDate=2018-09-22T00:00:00Z")
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk());

        verify(this.reservationsService).listReservationsWithinTimeRange(Instant.parse("2018-09-22T00:00:00Z"), null);

    }

    @Test
    public void testNumberOfDaysIsNotPresentShouldPassNullAndReturn200WhenDatesAreAvailable() throws Exception {

        this.mvc.perform(get("/v1/reservations?startDate=2018-09-22T00:00:00Z")
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk());

        verify(this.reservationsService).listReservationsWithinTimeRange(Instant.parse("2018-09-22T00:00:00Z"), null);

    }

    @Test
    public void testStartDateIsNotPresentShouldReturn400() throws Exception {
        this.mvc.perform(get("/v1/reservations")
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
    }

    @Test
    public void testStartDateIsEmptyShouldReturn400() throws Exception {
        this.mvc.perform(get("/v1/reservations?startDate=")
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest()).andExpect(content()
                .json(exConverter.toBuilder(new IllegalArgumentException(
                        "Start date parameter cannot be null or empty")).errorCode("400").wrap().toString()));
    }

    @Test
    public void testMakeReservationOnValidDatesShouldReturn200AndReservationId() throws Exception {

        User testUser = ReservationTestUtil.createUser("Florencia", "Prieto", "florpri@gmail.com");
        Reservation testReservation = ReservationTestUtil.createReservation(Instant.parse("2018-09-22T00:00:00Z"),
                Instant.parse("2018-09-23T00:00:00Z"), testUser);

        ReservationJson request = modelMapper.map(testReservation, ReservationJson.class);

        testReservation.setId(1L);
        ReservationJson expectedResponse = modelMapper.map(testReservation, ReservationJson.class);
        given(this.reservationsService.createReservation(testUser, request.getStartDate(), request.getEndDate()))
                .willReturn(testReservation);

        this.mvc.perform(post("/v1/reservations")
                .content(objectMapper.writeValueAsString(request)).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(content()
                .json(objectMapper.writeValueAsString(expectedResponse)));
    }

    @Test
    public void testMakeReservationOnValidDatesShouldReturn409AndErrorMessage() throws Exception {

        User testUser = ReservationTestUtil.createUser("Florencia", "Prieto", "florpri@gmail.com");
        Reservation testReservation = ReservationTestUtil.createReservation(Instant.parse("2018-09-22T00:00:00Z"),
                Instant.parse("2018-09-23T00:00:00Z"), testUser);

        ReservationJson request = modelMapper.map(testReservation, ReservationJson.class);

        given(this.reservationsService.createReservation(testUser, request.getStartDate(), request.getEndDate()))
                .willThrow(new IllegalStateException("Illegal State Error"));

        this.mvc.perform(post("/v1/reservations").content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(content().json(exConverter.toBuilder(new IllegalArgumentException(
                        "Illegal State Error")).errorCode("409").wrap().toString()));
    }

    @Test
    public void testUpdateReservationReturns200AndUpdatedReservation() throws Exception {

        User testUser = ReservationTestUtil.createUser("Florencia", "Prieto", "florpri@gmail.com");
        Reservation testReservation = ReservationTestUtil.createReservation(Instant.parse("2018-09-22T00:00:00Z"),
                Instant.parse("2018-09-23T00:00:00Z"), testUser);
        testReservation.setVersion(1L);
        testReservation.setId(1L);

        ReservationJson request = modelMapper.map(testReservation, ReservationJson.class);

        given(this.reservationsService.updateReservation(testReservation)).willReturn(testReservation);

        this.mvc.perform(put("/v1/reservations").content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(request)));
    }

    @Test
    public void testUpdateReservationFailReturns400AndErrorMessage() throws Exception {

        User testUser = ReservationTestUtil.createUser("Florencia", "Prieto", "florpri@gmail.com");
        Reservation testReservation = ReservationTestUtil.createReservation(Instant.parse("2018-09-22T00:00:00Z"),
                Instant.parse("2018-09-23T00:00:00Z"), testUser);

        ReservationJson request = modelMapper.map(testReservation, ReservationJson.class);

        given(this.reservationsService.updateReservation(testReservation))
                .willThrow(new IllegalArgumentException("Illegal Argument Error"));

        this.mvc.perform(put("/v1/reservations").content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(exConverter.toBuilder(new IllegalArgumentException(
                        "Illegal Argument Error")).errorCode("400").wrap().toString()));
    }

    @Test
    public void testDeleteReservationSucessfullyReturns204() throws Exception {

        User testUser = ReservationTestUtil.createUser("Florencia", "Prieto", "florpri@gmail.com");
        Reservation testReservation = ReservationTestUtil.createReservation(Instant.parse("2018-09-22T00:00:00Z"),
                Instant.parse("2018-09-23T00:00:00Z"), testUser);
        testReservation.setVersion(1L);
        testReservation.setId(1L);

        ReservationJson request = modelMapper.map(testReservation, ReservationJson.class);

        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);

        this.mvc.perform(delete("/v1/reservations").content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(this.reservationsService).deleteReservation(captor.capture());

        assertEquals("Deleted reservation ID has wrong value", captor.getValue().getId(), 1L);
    }

    @Test
    public void testDeleteReservationFailReturns400AndErrorMessage() throws Exception {

        User testUser = ReservationTestUtil.createUser("Florencia", "Prieto", "florpri@gmail.com");
        Reservation testReservation = ReservationTestUtil.createReservation(Instant.parse("2018-09-22T00:00:00Z"),
                Instant.parse("2018-09-23T00:00:00Z"), testUser);

        ReservationJson request = modelMapper.map(testReservation, ReservationJson.class);

        doThrow(new IllegalArgumentException("Illegal Argument Error")).when(this.reservationsService)
                .deleteReservation(testReservation);

        this.mvc.perform(delete("/v1/reservations").content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(exConverter.toBuilder(new IllegalArgumentException(
                        "Illegal Argument Error")).errorCode("400").wrap().toString()));
    }

    @SpringBootApplication
    static class TestConfiguration {
    }

}
