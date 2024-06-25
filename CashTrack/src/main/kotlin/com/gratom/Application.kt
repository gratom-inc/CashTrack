package com.gratom

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.jetty.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun main() {
    embeddedServer(Jetty, port = 7178, host = "0.0.0.0") {
        install(StatusPages) {
            exception<Throwable> { call, cause ->
                call.respondText(text = "500: $cause" , status = HttpStatusCode.InternalServerError)
            }
            status(HttpStatusCode.NotFound) { call, status ->
                call.respondText(text = "404: Not Found", status = status)
            }
        }
        install(ShutDownUrl.ApplicationCallPlugin) {
            shutDownUrl = "/shutdown"
            exitCodeSupplier = { 0 }
        }
        routing {
            get("/hello") {
                call.respondText("Hello: ${foo()}")
            }
        }
    }.start(wait = true)
}
