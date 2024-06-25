package com.gratom.cashtrack

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.gratom.cashtrack.assets.schwab.SchwabCheckingData
import com.gratom.cashtrack.assets.schwab.readSchwabCheckingCsv

fun buildObjectMapper(): ObjectMapper = ObjectMapper()
    .registerModule(JavaTimeModule())

val objectMapper = buildObjectMapper()

fun reportData() = SchwabCheckingData(
    readSchwabCheckingCsv(schwabCheckingConsolidatedCsvFilePath)
        .filterFrom(schwabCheckingCutoffDateA)
)

fun reportLogs(): ArrayList<String> {
    val data = reportData()
    val logs = data.logStrings(true)
    logs.add("\n")
    logs.addAll(data.logStrings())
    return logs
}

fun report(): ArrayList<String> {
    val startTime = System.currentTimeMillis()
    val logs = reportLogs()
    val endTime = System.currentTimeMillis()
    val timeTaken = endTime - startTime
    logs.add("\nProcessing took $timeTaken ms")
    return logs
}
