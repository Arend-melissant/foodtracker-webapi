package com.foodtracker.application.port.outbound;

public record GooglePrincipal(
        String subjectId,
        String email,
        String name,
        String pictureUrl
) {}

