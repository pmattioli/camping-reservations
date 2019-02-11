package com.pmattioli.campsite.reservations.controller.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.PRECONDITION_FAILED;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.ResponseEntity.status;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public final class ResponseUtils {

    private ResponseUtils() {}

    public static <T> ResponseEntity<T> ok(final T body) {
        return response(HttpStatus.OK.value(), body);
    }

    public static <T> ResponseEntity<T> notFound() {
        return response(HttpStatus.NOT_FOUND.value(), null);
    }

    public static <T> ResponseEntity<T> internalError(final T body) {
        return response(INTERNAL_SERVER_ERROR.value(), body);
    }

    public static <T> ResponseEntity<T> preconditionFail(final T body) {
        return response(PRECONDITION_FAILED.value(), body);
    }

    public static <T> ResponseEntity<T> badRequest(final T body) {
        return response(BAD_REQUEST.value(), body);
    }

    public static <T> ResponseEntity<T> conflict(final T body) {
        return response(CONFLICT.value(), body);
    }

    public static <T> ResponseEntity<T> response(final HttpStatus status, final T body) {
        return response(status.value(), body);
    }

    public static <T> ResponseEntity<T> response(final int status, final T body) {
        return status(status).contentType(APPLICATION_JSON).body(body);
    }

    public static ObjectNode object() {
        return JsonNodeFactory.instance.objectNode();
    }

    public static ArrayNode array() {
        return JsonNodeFactory.instance.arrayNode();
    }

}
