package com.foodtracker.infrastructure.adapter.inbound.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.time.Instant;

/**
 * RFC 7807 Problem Details error model supporting JSON and XML.
 */
@XmlRootElement(name = "problem")
@XmlAccessorType(XmlAccessType.FIELD)
public class ErrorResponseDto {

    @XmlElement(name = "title")
    @JsonProperty("title")
    private String title;

    @XmlElement(name = "status")
    @JsonProperty("status")
    private int status;

    @XmlElement(name = "detail")
    @JsonProperty("detail")
    private String detail;

    @XmlElement(name = "timestamp")
    @JsonProperty("timestamp")
    private String timestamp;

    public ErrorResponseDto() {}

    public ErrorResponseDto(String title, int status, String detail) {
        this.title = title;
        this.status = status;
        this.detail = detail;
        this.timestamp = Instant.now().toString();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}

