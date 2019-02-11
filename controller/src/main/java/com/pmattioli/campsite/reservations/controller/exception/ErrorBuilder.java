package com.pmattioli.campsite.reservations.controller.exception;

import com.fasterxml.jackson.databind.node.ObjectNode;

public final class ErrorBuilder {

    private String errorCode;
    private String message;

    public static ErrorBuilder from(final RuntimeException exception) {
        String message = exception.getMessage();
        return new ErrorBuilder().errorCode("500").uiMessage(message);
    }

    public ErrorBuilder errorCode(final String errorCode) {
        this.errorCode = errorCode;
        return this;
    }

    public ErrorBuilder uiMessage(final String uiMessage) {
        this.message = uiMessage;
        return this;
    }

    public ObjectNode build() {
        ObjectNode error = ResponseUtils.object();
        error.put("error_code", errorCode);
        if (message != null) {
            error.put("ui_message", message);
        }
        return error;
    }

    public ObjectNode wrap() {
        return new ErrorResponseBuilder().add(build()).build();
    }

}
