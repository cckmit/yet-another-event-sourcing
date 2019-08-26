package com.github.daggerok.yetanothereventsourcing

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class YetAnotherEventSourcingApplication

fun main(args: Array<String>) {
    runApplication<YetAnotherEventSourcingApplication>(*args)
}
