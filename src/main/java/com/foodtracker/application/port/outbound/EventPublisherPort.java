package com.foodtracker.application.port.outbound;

public interface EventPublisherPort {
    <T> void publish(T event);
}

