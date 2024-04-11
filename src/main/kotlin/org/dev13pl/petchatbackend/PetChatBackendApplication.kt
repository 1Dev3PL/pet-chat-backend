package org.dev13pl.petchatbackend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PetChatBackendApplication

fun main(args: Array<String>) {
    runApplication<PetChatBackendApplication>(*args)
}
