package com.gratom.server.plugins

import com.gratom.foo
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello: ${foo()}")
        }
    }
}
