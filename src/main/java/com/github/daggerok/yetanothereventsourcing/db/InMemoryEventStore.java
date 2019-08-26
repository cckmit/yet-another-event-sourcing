package com.github.daggerok.yetanothereventsourcing.db;

import com.github.daggerok.yetanothereventsourcing.user.DomainEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
public class InMemoryEventStore {

    private final Map<UUID, Collection<DomainEvent>> eventStore = new ConcurrentHashMap<>();

    public void set(@NotNull UUID key, @NotNull Collection<DomainEvent> domainEvents) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(domainEvents);
        eventStore.put(key, new CopyOnWriteArrayList<>(domainEvents));
    }

    public Collection<DomainEvent> get(@NotNull UUID key) {
        Objects.requireNonNull(key);
        return eventStore.getOrDefault(key, new CopyOnWriteArrayList<>());
    }
}
