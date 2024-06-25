package com.gratom.cashtrack

import com.gratom.cashtrack.assets.schwab.SchwabCheckingData
import com.gratom.cashtrack.assets.schwab.readSchwabCheckingCsv

fun process(): ArrayList<String> {
    val dataA = SchwabCheckingData(
        readSchwabCheckingCsv(schwabCheckingConsolidatedCsvFilePath)
            .filterFrom(schwabCheckingCutoffDateA)
    )
    val logs = dataA.logStrings(true)
    logs.add("\n")
    logs.addAll(dataA.logStrings())
    return logs
}

fun report(): ArrayList<String> {
    val startTime = System.currentTimeMillis()
    val logs = process()
    val endTime = System.currentTimeMillis()
    val timeTaken = endTime - startTime
    logs.add("\nProcessing took $timeTaken ms")
    return logs
}
