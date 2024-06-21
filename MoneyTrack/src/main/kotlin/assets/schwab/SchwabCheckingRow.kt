package assets.schwab

import TrDateRow
import io.github.rtmigo.dec.Dec
import readCsv
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

val dateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("M/d/yyyy")
val moneyFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.US)

fun parseDate(s: String): LocalDate = LocalDate.parse(s, dateFormat)
fun parseInt(s: String): Int? = if (s == "") null else s.toInt()
fun parseMoney(s: String): Dec? =
    if (s == "") null
    else if (s.first() == '(' && s.last() == ')')
        parseMoney(s.substring(1, s.length - 2))
    else Dec(moneyFormat.parse(s).toFloat().toBigDecimal())

enum class SchwabCheckingTransactionType {
    ACH,
    WIRE,
    CHECK,
    CREDIT,
    DEPOSIT,
    INTADJUST,
    TRANSFER,
    VISA,
    FEE,
    RETURNITEM,
    ATM,
    ATMREBATE
}

data class SchwabCheckingRow(
    override val trDate: LocalDate,
    val trType: SchwabCheckingTransactionType,
    val checkNum: Int?,
    val desc: String,
    val withdrawal: Dec?,
    val deposit: Dec?,
    val balance: Dec,
) : TrDateRow {
    constructor(
        trDate: String,
        trType: String,
        checkNum: String,
        desc: String,
        withdrawal: String,
        deposit: String,
        balance: String
    ) : this(
        parseDate(trDate),
        SchwabCheckingTransactionType.valueOf(trType),
        parseInt(checkNum),
        desc,
        parseMoney(withdrawal).let {
            if (it != null) {
                require(it > Dec.ZERO)
            }
            it
        },
        parseMoney(deposit).let {
            if (it != null) {
                require(it > Dec.ZERO)
            }
            it
        },
        parseMoney(balance)!!
    )

    fun cleanString(): String {
        val amt = if (deposit != null) "Dep: $deposit" else "Wdr: $withdrawal"
        return "$trDate | $amt | $desc"
    }

    val depositN get(): Dec = deposit ?: Dec.ZERO
    val withdrawalN get(): Dec = withdrawal ?: Dec.ZERO
}

fun makeSchwabCheckingRow(csvFields: List<String>): SchwabCheckingRow {
    require(csvFields.size == 7)
    val row = SchwabCheckingRow(
        csvFields[0],
        csvFields[1],
        csvFields[2],
        csvFields[3],
        csvFields[4],
        csvFields[5],
        csvFields[6]
    )
    require(row.deposit == null || row.withdrawal == null)
    return row
}

fun validateSchwabChecking(c: ArrayList<SchwabCheckingRow>) {
    var expectedBalance = c[0].balance
    for (i in 1..<c.size) {
        val row = c[i]

        if (row.withdrawal != null) {
            expectedBalance -= row.withdrawal
        }

        if (row.deposit != null) {
            expectedBalance += row.deposit
        }

        val diff = expectedBalance.compareTo(row.balance)
        if (diff != 0) {
            if (c[i + 1].desc == "Overdraft Protection Transfer" ||
                "Overdraft Transfer from Brokerage -5483" in c[i + 1].desc
            ) {
                continue
            }
            throw Exception(
                "Incorrect balance in CSV.\n" +
                        "Expected balance: ${expectedBalance}\n" +
                        "Calculated balance: ${row.balance}\n" +
                        "These numbers do not seem to match.\n" +
                        "Raw row: $row\n" +
                        "Next row: ${c[i + 1]}"
            )
        }
    }
}

fun readSchwabCheckingCsv(fileName: String): ArrayList<SchwabCheckingRow> {
    return readCsv(fileName, 7, ::makeSchwabCheckingRow, ::validateSchwabChecking)
}

