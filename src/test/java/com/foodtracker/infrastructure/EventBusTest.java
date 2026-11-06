package com.foodtracker.infrastructure;

import com.foodtracker.domain.event.FoodLoggedEvent;
import com.foodtracker.infrastructure.adapter.outbound.eventbus.InMemoryEventPublisher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class EventBusTest {

    @Test
    @DisplayName("Should deliver domain event to registered subscriber via internal message bus")
    void testEventBusDispatch() throws InterruptedException {
        InMemoryEventPublisher publisher = new InMemoryEventPublisher();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<FoodLoggedEvent> received = new AtomicReference<>();

        publisher.registerSubscriber(event -> {
            if (event instanceof FoodLoggedEvent fle) {
                received.set(fle);
                latch.countDown();
            }
        });

        FoodLoggedEvent event = new FoodLoggedEvent("f-1", "user-1", "Matcha Latte", Instant.now());
        publisher.publish(event);

        boolean completed = latch.await(2, TimeUnit.SECONDS);
        assertThat(completed).isTrue();
        assertThat(received.get()).isNotNull();
        assertThat(received.get().foodName()).isEqualTo("Matcha Latte");

        publisher.shutdown();
    }
}

