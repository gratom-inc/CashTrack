import assets.schwab.SchwabCheckingData
import assets.schwab.parseDate
import assets.schwab.readSchwabCheckingCsv
import com.github.labai.deci.Deci
import io.github.rtmigo.dec.Dec

val schwabCheckingCutoffDateA = parseDate("05/01/2022") // parseDate("10/23/2020")

fun process(): List<String> {
    val dataA = SchwabCheckingData(
        readSchwabCheckingCsv(schwabCheckingConsolidatedCsvFilePath)
            .filterFrom(schwabCheckingCutoffDateA)
    )

    val logs = dataA.logStrings(true) + "\n" + dataA.logStrings()
    return logs
}

fun main(args: Array<String>) {
    println("Processing...")

    val startTime = System.currentTimeMillis()
    val logs = process()
    val endTime = System.currentTimeMillis()
    val timeTaken = endTime - startTime

    logs.forEach {
        println(it)
    }
    println("\nProcessing took $timeTaken ms")

    val x1 = listOf<Deci>(Deci(1), Deci(2), Deci(3))
    val x2 = listOf<Dec>(Dec(1), Dec(2), Dec(3))
//    val x1s = x1.sumOf { it }
//    val x2s = x2.sumOf { it }
    println("x1: ${x1}")
    println("x2: ${x2}")

    // println("Hello World!")
    // Try adding program arguments via Run/Debug configuration.
    // Learn more about running applications: https://www.jetbrains.com/help/idea/running-applications.html.
    // println("Program arguments: ${args.joinToString()}")
}
