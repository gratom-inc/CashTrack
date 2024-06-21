package assets.schwab

import TransactionsCategorize.SchwabCheckingCategories.ccPayment
import TransactionsCategorize.SchwabCheckingCategories.identifyDepositCategory
import TransactionsCategorize.SchwabCheckingCategories.identifyWithdrawalCategory
import io.github.rtmigo.dec.Dec
import sum

class SchwabCheckingData(rows: List<SchwabCheckingRow>) {
    val deposits: SchwabCheckingGroups
    val creditCardWithdrawals: SchwabCheckingGroups
    val otherWithdrawals: SchwabCheckingGroups

    val firstRow: SchwabCheckingRow
    val startingBalance: Dec
    val lastRow: SchwabCheckingRow

    init {
        firstRow = rows.first()
        lastRow = rows.last()
        startingBalance = firstRow.balance - firstRow.depositN + firstRow.withdrawalN

        val (depositRows, withdrawalRows) = rows.bisectSchwabCheckingRows()
        deposits = SchwabCheckingGroups(depositRows) { identifyDepositCategory(it) }
        val (ccWithdrawalRows, otherWithdrawalRows) = withdrawalRows.partition {
            identifyWithdrawalCategory(it).contains(ccPayment)
        }

        creditCardWithdrawals = SchwabCheckingGroups(ccWithdrawalRows) { identifyWithdrawalCategory(it) }
        otherWithdrawals = SchwabCheckingGroups(otherWithdrawalRows) { identifyWithdrawalCategory(it) }

        sanityCheck()
    }

    private fun calcAppleInterest(): ArrayList<String> {
        val s = ArrayList<String>()
        if (deposits.groups.containsKey("Apple Savings Deposit") &&
            otherWithdrawals.groups.containsKey("Apple Savings Withdrawal")
        ) {
            val appleDepTotal = deposits.groups.getValue("Apple Savings Deposit").computeTotal()
            val appleWdrTotal = otherWithdrawals.groups.getValue("Apple Savings Withdrawal").computeTotal()
            if (appleDepTotal > appleWdrTotal) {
                val appleInterest = appleWdrTotal + appleDepTotal
                s += "Apple Savings Interest: $appleInterest"
            }
        }
        return s
    }

    private fun sanityCheck(returnLogs: Boolean = false): List<String> {
        val startingBalancePlusDepositsTotal = startingBalance + deposits.grandTotal
        val remainder = startingBalancePlusDepositsTotal +
                (creditCardWithdrawals.grandTotal + otherWithdrawals.grandTotal)
        require(remainder == lastRow.balance)

        val s = ArrayList<String>()
        if (returnLogs) {
            s += "Starting Balance: $startingBalance"
            s += "Starting Balance + Deposits Total: $startingBalancePlusDepositsTotal"
            s += "Last Row Balance: ${lastRow.balance} Remainder: $remainder"
        }
        return s
    }

    fun logStrings(thorough: Boolean = false): List<String> {
        val s = ArrayList<String>()

        s += sanityCheck(true)
        s += calcAppleInterest()

        s += "\nOther Withdrawals:\n~~~~~~~~~~~~~~~~~~~~~~~~"
        if (thorough) {
            s += otherWithdrawals.totalsStringsThorough()
            s += "Other Withdrawals Grand Total: " + otherWithdrawals.grandTotal
        } else {
            s += otherWithdrawals.totalsStrings()
        }

        s += "\nCredit Card Withdrawals:\n~~~~~~~~~~~~~~~~~~~~~~~~"
        if (thorough) {
            s += creditCardWithdrawals.totalsStringsThorough()
            s += "Credit Card Withdrawals Grand Total: " + creditCardWithdrawals.grandTotal
        } else {
            s += creditCardWithdrawals.totalsStrings()
        }

        s += "\nDeposits:\n~~~~~~~~~"
        if (thorough) {
            s += deposits.totalsStringsThorough()
            s += "Deposits Grand Total: " + deposits.grandTotal
        } else {
            s += deposits.totalsStrings()
        }

        return s
    }
}

class SchwabCheckingGroups(rows: List<SchwabCheckingRow>, categorizer: (SchwabCheckingRow) -> String) {
    val groups: MutableMap<String, ArrayList<SchwabCheckingRow>> = HashMap()

    init {
        rows.forEach {
            val category = categorizer(it)
            // Refactor: e.g. groups.getOrDefault(category, ArrayList()).add(it)
            if (category !in groups) {
                groups[category] = ArrayList()
            }
            groups.getValue(category).add(it)
        }
    }

    val grandTotal get(): Dec = computeGrandTotal()
    private fun computeGrandTotal(): Dec {
        val totals: List<Pair<String, Dec>> = computeTotals()
        val grandTotal: Dec = totals.map { it.second }.sum()
        return grandTotal
    }

    val totals get(): Map<String, Dec> = computeTotals().toMap()
    private fun computeTotals(): List<Pair<String, Dec>> =
        groups.map { (desc, rows) -> Pair(desc, rows.computeTotal()) }
            .sortedByDescending { it.second }

    fun totalsStrings(): List<String> {
        val s = ArrayList<String>()
        val totals = computeTotals()
        for ((desc, total) in totals) {
            s += "$desc: $total"
        }
        val grandTotal = totals.map { it.second }.sum()
        s += "Total: $grandTotal"
        return s
    }

    fun totalsStringsThorough(): List<String> {
        val s = ArrayList<String>()
        for ((desc, group) in groups) {
            s += "$desc:"
            s += "-".repeat(desc.length + 1)
            s += group.cleanStringWithTotal()
            s += "\n"
        }
        return s
    }
}

fun List<SchwabCheckingRow>.bisectSchwabCheckingRows(): Pair<ArrayList<SchwabCheckingRow>, ArrayList<SchwabCheckingRow>> =
    Pair(
        ArrayList(this.filter { it.deposit != null }),
        ArrayList(this.filter { it.withdrawal != null })
    )

fun List<SchwabCheckingRow>.cleanStringWithTotal(): List<String> {
    return this.cleanStrings() + "Total: ${this.computeTotal()}"
}

fun List<SchwabCheckingRow>.cleanStrings(): List<String> {
    return this.map {
        it.cleanString()
    }
}

fun List<SchwabCheckingRow>.computeTotal(): Dec {
    val depositTotal = map { it.deposit ?: Dec.ZERO }.sum()
    val withdrawalTotal = map { it.withdrawal ?: Dec.ZERO }.sum()
    return depositTotal - withdrawalTotal
}
