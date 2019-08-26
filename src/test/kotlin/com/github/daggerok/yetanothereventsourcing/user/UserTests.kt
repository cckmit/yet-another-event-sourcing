package com.github.daggerok.yetanothereventsourcing.user

import com.github.daggerok.yetanothereventsourcing.db.InMemoryEventStore
import org.assertj.core.api.Assertions.*
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import java.util.*

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserTests {

    private val eventStore = InMemoryEventStore()
    private val userCommandHandler = UserCommandHandler(eventStore)

    @Test
    fun `should create user`() {
        // given:
        val aggregateId = UUID.randomUUID()
        // when:
        userCommandHandler.handle(CreateUser(aggregateId, "test-user"))
        // then:
        assertThat(eventStore[aggregateId]).hasSize(1)
    }

    @Test
    fun `should not invite if user is not known 1`() {
        // when:
        val failure = assertThatThrownBy {
            userCommandHandler.handle(InviteUser(UUID.randomUUID(), UUID.randomUUID(), "hola"))
        }
        // then:
        failure.extracting {
            assertThat(it).isInstanceOf(IllegalStateException::class.java)
        }
        // or:
        failure.isInstanceOf(IllegalStateException::class.java)
        // and:
        failure.hasMessageEndingWith("not known.")
    }

    @Test
    fun `should not invite if user is not known 2`() {
        // when:
        val error = assertThatThrownBy {
            userCommandHandler.handle(InviteUser(UUID.randomUUID(), UUID.randomUUID(), "hola"))
        }
        // then:
        error.hasMessageContaining("is not known.")
    }

    @Test(expected = IllegalStateException::class)
    fun `should not invite themselves`() {
        // given:
        val aggregateId = UUID.randomUUID()
        // and:
        userCommandHandler.handle(CreateUser(aggregateId, "test-user"))
        // when:
        userCommandHandler.handle(InviteUser(aggregateId, aggregateId, "hola"))
        // expect:
        fail<String>("IllegalStateException should be thrown.")
    }

    @Test
    fun `should send invite request`() {
        // given:
        val aggregateId = UUID.randomUUID()
        userCommandHandler.handle(CreateUser(aggregateId, "test-user"))
        // when:
        userCommandHandler.handle(InviteUser(aggregateId, UUID.randomUUID(), "hola"))
        // then:
        assertThat(eventStore[aggregateId]).hasSize(2)
    }

    @Test
    fun `should decline invite request`() {
        // given:
        val aggregateId = UUID.randomUUID()
        userCommandHandler.handle(CreateUser(aggregateId, "test-user"))
        // when:
        userCommandHandler.handle(DeclineInvite(aggregateId, UUID.randomUUID(), "sorry..."))
        // then:
        assertThat(eventStore[aggregateId]).hasSize(2)
    }

    @Test
    fun `should accept invite request`() {
        // given:
        val aggregateId = UUID.randomUUID()
        userCommandHandler.handle(CreateUser(aggregateId, "test-user"))
        // when:
        userCommandHandler.handle(AcceptInvite(aggregateId, UUID.randomUUID()))
        // then:
        assertThat(eventStore[aggregateId]).hasSize(2)
    }
}
