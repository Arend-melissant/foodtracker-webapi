package com.foodtracker.infrastructure.adapter.outbound.eventbus;

import com.foodtracker.application.port.outbound.EventPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Internal in-memory message bus implementing EventPublisherPort.
 * Decouples cross-cutting reactions and asynchronous notifications from core business transactions.
 */
public class InMemoryEventPublisher implements EventPublisherPort {
    private static final Logger logger = LoggerFactory.getLogger(InMemoryEventPublisher.class);

    private final List<Consumer<Object>> subscribers = new CopyOnWriteArrayList<>();
    private final ExecutorService executor = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "internal-event-bus-worker");
        t.setDaemon(true);
        return t;
    });

    public InMemoryEventPublisher() {
        // Register default audit logger subscriber
        registerSubscriber(event -> {
            logger.info("[EventBus Audit] Dispatched domain event: {} -> {}",
                    event.getClass().getSimpleName(), event);
        });
    }

    public void registerSubscriber(Consumer<Object> subscriber) {
        subscribers.add(subscriber);
    }

    @Override
    public <T> void publish(T event) {
        if (event == null) return;
        for (Consumer<Object> subscriber : subscribers) {
            executor.submit(() -> {
                try {
                    subscriber.accept(event);
                } catch (Exception e) {
                    logger.error("Error executing subscriber for event {}: {}",
                            event.getClass().getSimpleName(), e.getMessage(), e);
                }
            });
        }
    }

    public void shutdown() {
        executor.shutdown();
    }
}

