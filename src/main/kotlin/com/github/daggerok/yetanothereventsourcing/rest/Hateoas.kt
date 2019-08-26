@file:JvmName("Hateoas")

package com.github.daggerok.yetanothereventsourcing.rest

import org.springframework.web.reactive.function.server.ServerRequest

fun ServerRequest.baseUrl(vararg paths: String): String {
    val uri = this.uri()
    val path = paths.joinToString("/")
    return "${uri.scheme}://${uri.authority}$path"
}
