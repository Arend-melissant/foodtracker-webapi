package com.foodtracker.infrastructure.adapter.inbound.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "authRequest")
@XmlAccessorType(XmlAccessType.FIELD)
public class AuthRequestDto {

    @XmlElement(name = "idToken")
    @JsonProperty("idToken")
    private String idToken;

    // Optional for local dev convenience
    @XmlElement(name = "email")
    @JsonProperty("email")
    private String email;

    @XmlElement(name = "name")
    @JsonProperty("name")
    private String name;

    public AuthRequestDto() {}

    public AuthRequestDto(String idToken, String email, String name) {
        this.idToken = idToken;
        this.email = email;
        this.name = name;
    }

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

