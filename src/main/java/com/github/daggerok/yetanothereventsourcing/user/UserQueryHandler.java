package com.github.daggerok.yetanothereventsourcing.user;

import com.github.daggerok.yetanothereventsourcing.db.InMemoryEventStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class UserQueryHandler {

    private final InMemoryEventStore eventStore;

    public UserState handle(LoadUserQuery loadUserQuery) {
        Collection<DomainEvent> domainEvents = eventStore.get(loadUserQuery.getAggregateId());
        UserAggregate userAggregate = new UserAggregate(new UserState(), domainEvents);
        return userAggregate.getState();
    }
}
