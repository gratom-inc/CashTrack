import assets.schwab.SchwabCheckingData
import assets.schwab.parseDate
import assets.schwab.readSchwabCheckingCsv

val schwabCheckingCutoffDateA = parseDate("05/01/2022") // parseDate("10/23/2020")

fun process(): List<String> {
    val dataA = SchwabCheckingData(
        readSchwabCheckingCsv(schwabCheckingConsolidatedCsvFilePath)
            .filterFrom(schwabCheckingCutoffDateA)
    )

    val logs = dataA.logStrings(true) + "\n" + dataA.logStrings()
    return logs
}

fun main() {
    println("Processing...")

    val startTime = System.currentTimeMillis()
    val logs = process()
    val endTime = System.currentTimeMillis()
    val timeTaken = endTime - startTime

    logs.forEach {
        println(it)
    }
    println("\nProcessing took $timeTaken ms")
}
