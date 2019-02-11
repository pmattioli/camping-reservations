package com.pmattioli.campsite.reservations.controller;

import static com.pmattioli.campsite.reservations.controller.exception.ResponseUtils.badRequest;
import static com.pmattioli.campsite.reservations.controller.exception.ResponseUtils.conflict;
import static com.pmattioli.campsite.reservations.controller.exception.ResponseUtils.internalError;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.time.Instant;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.pmattioli.campsite.reservations.controller.configuration.ReservationControllerConfiguration;
import com.pmattioli.campsite.reservations.controller.exception.ExceptionConverter;
import com.pmattioli.campsite.reservations.controller.model.ReservationJson;
import com.pmattioli.campsite.reservations.data.repo.Reservation;
import com.pmattioli.campsite.reservations.data.repo.User;
import com.pmattioli.campsite.reservations.service.ReservationsService;

@RestController
@RequestMapping("/v1/reservations")
@Import(ReservationControllerConfiguration.class)
public class ReservationController {

    private static final Logger LOG = LoggerFactory.getLogger(ReservationController.class);

    private final ExceptionConverter exConverter = new ExceptionConverter(this);

    @Autowired
    private ReservationsService reservationsService;

    @Autowired
    private ModelMapper modelMapper;

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ReservationJson>> listReservationsWithinTimeRange(@RequestParam(value="startDate") String startDate,
            @RequestParam(value="numberOfDays", required = false) Integer numberOfDays) {

        Assert.isTrue(startDate.length() > 0, "Start date parameter cannot be null or empty");

        List<Reservation> reservations =
                reservationsService.listReservationsWithinTimeRange(Instant.parse(startDate), numberOfDays);

        return ResponseEntity.ok(modelMapper.map(reservations, new TypeToken<List<ReservationJson>>() {}.getType()));

    }

    @PostMapping(consumes =  APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<ReservationJson> createReservation(@RequestBody ReservationJson request) {

        User user = modelMapper.map(request.getUser(), User.class);

        Reservation reservation =
                reservationsService.createReservation(user, request.getStartDate(), request.getEndDate());

        return ResponseEntity.ok(modelMapper.map(reservation, ReservationJson.class));

    }

    @PutMapping(consumes =  APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<ReservationJson> updateReservation(@RequestBody ReservationJson request) {

        Reservation updatedReservation =
                reservationsService.updateReservation(modelMapper.map(request, Reservation.class));

        return ResponseEntity.ok(modelMapper.map(updatedReservation, ReservationJson.class));

    }

    @DeleteMapping(consumes =  APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<ReservationJson> deleteReservation(@RequestBody ReservationJson request) {

        reservationsService.deleteReservation(modelMapper.map(request, Reservation.class));

        return ResponseEntity.noContent().build();

    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<JsonNode> handleIllegalArgumentException(final IllegalArgumentException ex) {
        return badRequest(exConverter.toBuilder(ex).errorCode("400").wrap());
    }

    @ExceptionHandler({IllegalStateException.class, ObjectOptimisticLockingFailureException.class})
    public ResponseEntity<JsonNode> handleIllegalStateException(final RuntimeException ex) {
        return conflict(exConverter.toBuilder(ex).errorCode("409").wrap());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity handleUnexpectedException(final RuntimeException ex) {
        LOG.error("An unexpected error occurred: ", ex);
        return internalError(exConverter.toBuilder(ex).errorCode("500").wrap());
    }
}
