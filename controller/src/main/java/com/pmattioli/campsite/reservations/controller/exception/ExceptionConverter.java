package com.pmattioli.campsite.reservations.controller.exception;

import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public final class ExceptionConverter {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final String serviceName;

    public ExceptionConverter(final Object service) {
        this.serviceName = service.getClass().getSimpleName();
    }

    public ErrorBuilder toBuilder(final RuntimeException exception) {
        return ErrorBuilder.from(exception);
    }

    public ObjectNode toObject(final RestClientResponseException exception) {
        try {
            return MAPPER.readValue(exception.getResponseBodyAsString(), ObjectNode.class);
        } catch (Exception ex) {
            return toBuilder(exception).errorCode(Integer.toString(exception.getRawStatusCode())).wrap();
        }
    }

}
