package com.gratom.cashtrack.server

import com.gratom.cashtrack.income.analyzeIncome
import com.gratom.cashtrack.objectMapper
import com.gratom.cashtrack.report
import com.gratom.cashtrack.reportData
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.html.body
import kotlinx.html.h1

fun Application.configureRouting() {
    routing {
        get("/hello") {
            call.respondHtml(HttpStatusCode.OK) {
                body {
                    h1 {
                        +"Hello World!"
                    }
                }
            }
        }
        get("/income") {
            val incomeHtml = analyzeIncome()
            call.respondText(
                "<pre>\n$incomeHtml\n</pre>",
                status = HttpStatusCode.OK,
                contentType = ContentType.Text.Html.withCharset(Charsets.UTF_8)
            )
        }
        get("/report") {
            val report = report().joinToString("\n")
            call.respondText(
                "<pre>\n$report\n</pre>",
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
