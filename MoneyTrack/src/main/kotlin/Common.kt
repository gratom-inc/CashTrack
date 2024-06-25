import io.github.rtmigo.dec.Dec
import io.github.rtmigo.dec.sumOf
import java.text.NumberFormat
import java.time.LocalDate
import java.util.*

interface TrDateRow {
    val trDate: LocalDate
}

fun <T : TrDateRow> ArrayList<out T>.filterFrom(cutoffDate: LocalDate): List<T> {
    return this.filter { it.trDate >= cutoffDate }
}

fun Iterable<Dec>.sum(): Dec = sumOf { it }

val numberFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.US)
fun Dec.f() = numberFormat.format(decimal)
