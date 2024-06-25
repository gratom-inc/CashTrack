package com.gratom.cashtrack.server

import com.gratom.cashtrack.foo
import com.gratom.cashtrack.report
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/hello") {
            call.respondText("Hello: ${foo()}", status = HttpStatusCode.OK)
        }
        get("/report") {
            val report = report().joinToString("\n")
            call.respondText("<pre>\n" +
                    "$report\n" +
                    "</pre>",
                status = HttpStatusCode.OK,
                contentType = ContentType.Text.Html.withCharset(Charsets.UTF_8))
        }
    }
}
