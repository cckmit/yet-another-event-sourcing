package com.github.daggerok.yetanothereventsourcing.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.vavr.API;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.Predicates.instanceOf;

@Getter
@Log4j2
@NoArgsConstructor
@AllArgsConstructor
public class UserState implements State {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private UUID aggregateId;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String username;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private UserStatus status = UserStatus.PENDING;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Collection<UUID> friends = new CopyOnWriteArrayList<>();

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Collection<UUID> friendRequests = new CopyOnWriteArrayList<>();

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<UUID, Collection<Message>> messages = new ConcurrentHashMap<>();

    @Override
    public void mutate(@NotNull DomainEvent... domainEvents) {
        Objects.requireNonNull(domainEvents);
        for (DomainEvent domainEvent : domainEvents) {
            API.Match(domainEvent).of(
                    // user
                    Case($(instanceOf(UserCreated.class)), this::on),
                    Case($(instanceOf(UserSuspended.class)), this::on),
                    Case($(instanceOf(UserReactivated.class)), this::on),
                    // friend
                    Case($(instanceOf(UserInvited.class)), this::on),
                    Case($(instanceOf(InviteDeclined.class)), this::on),
                    Case($(instanceOf(InviteAccepted.class)), this::on),
                    // message
                    Case($(instanceOf(MessageSent.class)), this::on),
                    Case($(instanceOf(MessageReceived.class)), this::on),
                    // fallback
                    Case($(), this::fallbackOnUnexpected)
            );
        }
    }

    /* user */

    public UserState on(UserCreated event) {
        aggregateId = event.getAggregateId();
        username = event.getUsername();
        status = UserStatus.ACTIVE;
        return this;
    }

    public UserState on(UserSuspended event) {
        status = UserStatus.SUSPENDED;
        return this;
    }

    public UserState on(UserReactivated event) {
        status = UserStatus.ACTIVE;
        return this;
    }

    /* invite */

    public UserState on(UserInvited event) {
        friendRequests.add(event.getUserId());
        return this;
    }

    public UserState on(InviteAccepted event) {
        friendRequests.removeIf(uuid -> uuid.equals(event.getRequesterId()));
        friends.add(event.getRequesterId());
        return this;
    }

    public UserState on(InviteDeclined event) {
        friendRequests.removeIf(uuid -> uuid.equals(event.getRequesterId()));
        return this;
    }

    /* message */

    public UserState on(MessageSent event) {
        return onMessage(event.getMessage().getToId(), event.getMessage());
    }

    public UserState on(MessageReceived event) {
        return onMessage(event.getMessage().getFromId(), event.getMessage());
    }

    private UserState onMessage(UUID id, Message message) {
        messages.putIfAbsent(id, new CopyOnWriteArrayList<>());
        Collection<Message> pastHistory = this.messages.get(id);
        Collection<Message> allInDescOrder = Stream.concat(Stream.of(message), pastHistory.stream())
                                                   .collect(Collectors.toCollection(CopyOnWriteArrayList::new));
        messages.put(id, new CopyOnWriteArrayList<>(allInDescOrder));
        return this;
    }

    /* fallback */

    public <E extends DomainEvent> UserState fallbackOnUnexpected(E event) {
        log.warn("unexpected event occur: {}", event);
        return this;
    }
}
