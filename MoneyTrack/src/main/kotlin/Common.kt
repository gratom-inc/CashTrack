import io.github.rtmigo.dec.Dec
import io.github.rtmigo.dec.sumOf
import java.time.LocalDate

interface TrDateRow {
    val trDate: LocalDate
}

fun <T : TrDateRow> ArrayList<out T>.filterFrom(cutoffDate: LocalDate): List<T> {
    return this.filter { it.trDate >= cutoffDate }
}

fun decSum(decs: Iterable<Dec>): Dec {
    return decs.sumOf { it }
}
