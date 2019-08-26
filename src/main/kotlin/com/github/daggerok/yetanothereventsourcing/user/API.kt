@file:JvmName("API.java")

package com.github.daggerok.yetanothereventsourcing.user

import java.time.ZonedDateTime
import java.util.*

@FunctionalInterface
interface Aggregate<E : DomainEvent> {
    fun apply(vararg domainEvents: E)
}

@FunctionalInterface
interface State<E : DomainEvent> {
    fun mutate(vararg domainEvents: E)
}

@FunctionalInterface
interface CommandHandler<C : Command> {
    fun handle(command: C)
}

/* commands */

sealed class Command(aggregateId: UUID, val at: ZonedDateTime = ZonedDateTime.now())

data class CreateUser(val aggregateId: UUID, val username: String) : Command(aggregateId)
data class SuspendUser(val aggregateId: UUID, val reason: String) : Command(aggregateId)
data class ReactivateUser(val aggregateId: UUID) : Command(aggregateId)

data class InviteUser(val aggregateId: UUID, val userId: UUID, val message: String) : Command(aggregateId)
data class DeclineInvite(val aggregateId: UUID, val requesterId: UUID, val reason: String) : Command(aggregateId)
data class AcceptInvite(val aggregateId: UUID, val requesterId: UUID) : Command(aggregateId)

data class SendMessage(val aggregateId: UUID, val message: Message) : Command(aggregateId)
data class ReceiveMessage(val aggregateId: UUID, val message: Message) : Command(aggregateId)

/* domain events */

sealed class DomainEvent(aggregateId: UUID, val at: ZonedDateTime = ZonedDateTime.now())

data class UserCreated(val aggregateId: UUID, val username: String) : DomainEvent(aggregateId)
data class UserSuspended(val aggregateId: UUID, val reason: String) : DomainEvent(aggregateId)
data class UserReactivated(val aggregateId: UUID) : DomainEvent(aggregateId)

data class UserInvited(val aggregateId: UUID, val userId: UUID, val message: String) : DomainEvent(aggregateId)
data class InviteDeclined(val aggregateId: UUID, val requesterId: UUID, val reason: String) : DomainEvent(aggregateId)
data class InviteAccepted(val aggregateId: UUID, val requesterId: UUID) : DomainEvent(aggregateId)

data class MessageSent(val aggregateId: UUID, val message: Message) : DomainEvent(aggregateId)
data class MessageReceived(val aggregateId: UUID, val message: Message) : DomainEvent(aggregateId)

/* queries */

sealed class Query(aggregateId: UUID)

data class LoadUserQuery(val aggregateId: UUID) : Query(aggregateId)

/***/

enum class UserStatus {
    PENDING, ACTIVE, SUSPENDED
}

enum class MessageType {
    SENT, RECEIVED
}

data class Message(
        val fromId: UUID,
        val toId: UUID,
        val body: String,
        val type: MessageType
)
