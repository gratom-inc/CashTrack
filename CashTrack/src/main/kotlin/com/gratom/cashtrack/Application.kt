package com.gratom.cashtrack

import com.gratom.cashtrack.server.configureAdministration
import com.gratom.cashtrack.server.configureRouting
import com.gratom.cashtrack.server.configureStatusPages
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.jetty.*

fun main() {
    embeddedServer(Jetty, port = 7178, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureAdministration()
    configureStatusPages()
    configureRouting()
}
