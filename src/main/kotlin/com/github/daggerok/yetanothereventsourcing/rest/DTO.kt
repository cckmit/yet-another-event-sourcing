@file:JvmName("DTO.java")

package com.github.daggerok.yetanothereventsourcing.rest

import com.github.daggerok.yetanothereventsourcing.user.UserState
import java.util.*

sealed class DTO

data class CreateUserRequest(val username: String, val id: UUID = UUID.randomUUID()) : DTO()
data class LoadUserResponse(val state: UserState) : DTO()
