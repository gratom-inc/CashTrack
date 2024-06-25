package com.gratom.cashtrack.server

import com.gratom.cashtrack.objectMapper
import com.gratom.cashtrack.report
import com.gratom.cashtrack.reportData
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/report") {
            val report = report().joinToString("\n")
            call.respondText(
                "<pre>\n" +
                        "$report\n" +
                        "</pre>",
                status = HttpStatusCode.OK,
                contentType = ContentType.Text.Html.withCharset(Charsets.UTF_8)
            )
        }
        get("/reportJson") {
            val report = reportData()
            val reportJson = objectMapper.writeValueAsString(report)
            call.respondText(
                reportJson,
                status = HttpStatusCode.OK,
                contentType = ContentType.Application.Json.withCharset(Charsets.UTF_8)
            )
        }
    }
}
