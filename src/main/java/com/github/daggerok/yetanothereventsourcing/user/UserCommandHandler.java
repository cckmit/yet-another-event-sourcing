package com.github.daggerok.yetanothereventsourcing.user;

import com.github.daggerok.yetanothereventsourcing.db.InMemoryEventStore;
import io.vavr.API;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.UUID;
import java.util.function.Consumer;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.Predicates.instanceOf;

@Log4j2
@Getter
@Service
@RequiredArgsConstructor
public class UserCommandHandler implements CommandHandler {

    private final InMemoryEventStore eventStore;

    @Override
    public void handle(@NotNull Command command) {
        API.Match(command).of(
                // user
                Case($(instanceOf(CreateUser.class)), this::handle),
                Case($(instanceOf(SuspendUser.class)), this::handle),
                Case($(instanceOf(ReactivateUser.class)), this::handle),
                // friend
                Case($(instanceOf(InviteUser.class)), this::handle),
                Case($(instanceOf(DeclineInvite.class)), this::handle),
                Case($(instanceOf(AcceptInvite.class)), this::handle),
                // message
                Case($(instanceOf(SendMessage.class)), this::handle),
                Case($(instanceOf(ReceiveMessage.class)), this::handle),
                // fallback
                Case($(), this::handleUnsupported)
        );
    }

    /* user */

    public Void handle(CreateUser command) {
        return update(command.getAggregateId(),
                      userAggregate -> userAggregate.createUser(command.getAggregateId(),
                                                                command.getUsername()));
    }

    public Void handle(SuspendUser command) {
        return update(command.getAggregateId(),
                      userAggregate -> userAggregate.suspendUser(command.getReason()));
    }

    public Void handle(ReactivateUser command) {
        return update(command.getAggregateId(), UserAggregate::reactivateUser);
    }

    /* friend */

    public Void handle(InviteUser command) {
        return update(command.getAggregateId(),
                      userAggregate -> userAggregate.sendFriendRequest(command.getUserId(),
                                                                       command.getMessage()));
    }

    public Void handle(DeclineInvite command) {
        return update(command.getAggregateId(),
                      userAggregate -> userAggregate.declineFriendRequest(command.getRequesterId(),
                                                                          command.getReason()));
    }

    public Void handle(AcceptInvite command) {
        return update(command.getAggregateId(),
                      userAggregate -> userAggregate.acceptFriendRequest(command.getRequesterId()));
    }

    /* message */

    public Void handle(SendMessage command) {
        return update(command.getAggregateId(),
                      userAggregate -> userAggregate.sendMessage(command.getMessage()));
    }

    public Void handle(ReceiveMessage command) {
        return update(command.getAggregateId(),
                      userAggregate -> userAggregate.receiveMessage(command.getMessage()));
    }

    /* fallback */

    public Void handleUnsupported(Command command) {
        log.warn("handle unsupported command: {}", command);
        return null;
    }

    /* update eventStore */

    private Void update(UUID aggregateId, Consumer<UserAggregate> action) {
        Collection<DomainEvent> pastEvents = eventStore.get(aggregateId);
        UserAggregate aggregate = new UserAggregate(new UserState(), pastEvents);
        action.accept(aggregate);
        eventStore.set(aggregateId, aggregate.getEventStream());
        return null;
    }
}
