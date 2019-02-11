package com.pmattioli.campsite.reservations.controller.exception;

import static com.pmattioli.campsite.reservations.controller.exception.ResponseUtils.array;
import static com.pmattioli.campsite.reservations.controller.exception.ResponseUtils.object;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.node.ObjectNode;

public final class ErrorResponseBuilder {

    private List<ObjectNode> errors = new ArrayList<>();

    public ErrorResponseBuilder add(final ObjectNode error) {
        errors.add(error);
        return this;
    }

    public boolean isEmpty() {
        return errors.isEmpty();
    }

    public ObjectNode build() {
        return (ObjectNode) object().set("errors", array().addAll(errors));
    }

}
