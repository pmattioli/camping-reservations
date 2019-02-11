package com.pmattioli.campsite.reservations.controller.model;

import java.time.Instant;

import javax.persistence.Version;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ReservationJson {

    @JsonProperty("booking_id")
    private String id;

    @JsonProperty("version")
    private String version;

    @JsonProperty("user")
    private User user;

    @JsonProperty("start_date")
    private Instant startDate;

    @JsonProperty("end_date")
    private Instant endDate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Instant getStartDate() {
        return startDate;
    }

    public void setStartDate(Instant startDate) {
        this.startDate = startDate;
    }

    public Instant getEndDate() {
        return endDate;
    }

    public void setEndDate(Instant endDate) {
        this.endDate = endDate;
    }
}
