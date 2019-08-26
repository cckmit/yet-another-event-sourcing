package com.github.daggerok.yetanothereventsourcing.user;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
public class UserAggregate implements Aggregate {

    private UserState state;
    private Collection<DomainEvent> eventStream;

    /* aggregate events */

    @Override
    public void apply(@NotNull DomainEvent... domainEvents) {
        Objects.requireNonNull(domainEvents);

        for (DomainEvent domainEvent : domainEvents) {
            state.mutate(domainEvent);
            eventStream.add(domainEvent);
        }
    }

    /* creation */

    public UserAggregate() { // 1
        this(new UserState(), new CopyOnWriteArrayList<>());
    }

    public UserAggregate(UserState snapshot, Collection<DomainEvent> domainEvents) { // 2
        this(snapshot, domainEvents.toArray(new DomainEvent[0]));
        // this(snapshot, Objects.isNull(domainEvents) ? new DomainEvent[0]
        //         : domainEvents.toArray(new DomainEvent[0]));
        // : domainEvents.toArray(new DomainEvent[domainEvents.size()]));
    }

    public UserAggregate(UserState snapshot, DomainEvent... domainEvents) { // 3
        Objects.requireNonNull(snapshot);
        Objects.requireNonNull(domainEvents);

        eventStream = new CopyOnWriteArrayList<>();
        state = new UserState(snapshot.getAggregateId(),
                              snapshot.getUsername(),
                              snapshot.getStatus(),
                              snapshot.getFriends(),
                              snapshot.getFriendRequests(),
                              snapshot.getMessages());
        apply(domainEvents);
    }

    /* user commands */

    public void createUser(UUID aggregateId, String username) {
        Objects.requireNonNull(aggregateId);
        Objects.requireNonNull(username);

        if (Objects.nonNull(state.getAggregateId()))
            throw new IllegalStateException("aggregateId already assigned.");
        if (Objects.nonNull(state.getUsername()))
            throw new IllegalStateException("username already assigned.");

        apply(new UserCreated(aggregateId, username));
    }

    public void suspendUser(String reason) {
        Objects.requireNonNull(reason);
        requireKnownUser();
            requireStatus(UserStatus.ACTIVE);
        apply(new UserSuspended(state.getAggregateId(), reason));
    }

    public void reactivateUser() {
        requireKnownUser();
        requireStatus(UserStatus.SUSPENDED);
        apply(new UserReactivated(state.getAggregateId()));
    }

    /* friend commands */

    public void sendFriendRequest(UUID friendId, String message) {
        Objects.requireNonNull(friendId);
        Objects.requireNonNull(message);

        requireKnownUser();
        if (state.getAggregateId().equals(friendId))
            throw new IllegalStateException("users cannot send friend requests to themselves.");

        apply(new UserInvited(state.getAggregateId(), friendId, message));
    }

    public void declineFriendRequest(UUID friendId, String reason) {
        Objects.requireNonNull(friendId);
        Objects.requireNonNull(reason);
        requireKnownUser();
        apply(new InviteDeclined(state.getAggregateId(), friendId, reason));
    }

    public void acceptFriendRequest(UUID friendId) {
        Objects.requireNonNull(friendId);
        requireKnownUser();
        apply(new InviteAccepted(state.getAggregateId(), friendId));
    }

    /* message commands */

    public void sendMessage(Message message) {
        Objects.requireNonNull(message);

        requireKnownUser();
        if (!state.getAggregateId().equals(message.getFromId()))
            throw new IllegalStateException("incorrect sender.");

        apply(new MessageSent(state.getAggregateId(), message));
    }

    public void receiveMessage(Message message) {
        Objects.requireNonNull(message);

        requireKnownUser();
        if (!state.getAggregateId().equals(message.getToId()))
            throw new IllegalStateException("incorrect recipient.");

        apply(new MessageReceived(state.getAggregateId(), message));
    }

    private void requireKnownUser() {
        if (Objects.isNull(state.getAggregateId()))
            throw new IllegalStateException("user is not known.");
    }

    private void requireStatus(UserStatus status) {
        Objects.requireNonNull(status);

        if (!status.equals(state.getStatus()))
            throw new IllegalStateException(String.format("user has unexpected status: %s.", status.name()));
    }
}
