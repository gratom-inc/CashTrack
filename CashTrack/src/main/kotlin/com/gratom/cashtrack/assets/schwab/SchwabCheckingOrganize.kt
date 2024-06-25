package com.gratom.cashtrack.assets.schwab

import com.gratom.cashtrack.TransactionsCategorize.SchwabCheckingCategories.ccPayment
import com.gratom.cashtrack.TransactionsCategorize.SchwabCheckingCategories.identifyDepositCategory
import com.gratom.cashtrack.TransactionsCategorize.SchwabCheckingCategories.identifyWithdrawalCategory
import com.gratom.cashtrack.f
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.*

class SchwabCheckingData(rows: List<SchwabCheckingRow>) {
    val deposits: SchwabCheckingGroups
    val creditCardWithdrawals: SchwabCheckingGroups
    val otherWithdrawals: SchwabCheckingGroups

    val firstRow: SchwabCheckingRow
    val startingBalance: BigDecimal
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

    fun logStrings(thorough: Boolean = false): ArrayList<String> {
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
    val groups: MutableMap<String, SchwabCheckingGroup> = HashMap()

    init {
        rows.forEach {
            val groupName = categorizer(it)
            // Refactor: e.g. groups.getOrDefault(category, ArrayList()).add(it)
            if (groupName !in groups) {
                groups[groupName] = SchwabCheckingGroup(groupName, ArrayList())
            }
            groups.getValue(groupName).rows.add(it)
        }
    }

    val grandTotal get(): BigDecimal = computeGrandTotal()
    private fun computeGrandTotal(): BigDecimal {
        val totals: List<Pair<String, BigDecimal>> = computeTotals()
        val grandTotal: BigDecimal = totals.map { it.second }.sumOf { it }
        return grandTotal
    }

    val totals get(): Map<String, BigDecimal> = computeTotals().toMap()
    private fun computeTotals(): List<Pair<String, BigDecimal>> =
        groups.map { (_, group) -> Pair(group.groupName, group.computeTotal()) }
            .sortedByDescending { it.second }

    fun totalsStrings(): List<String> {
        val s = ArrayList<String>()
        val totals = computeTotals()
        val nf = NumberFormat.getCurrencyInstance(Locale.US)
        for ((desc, total) in totals) {
            s += "$desc: ${total.f()}"
        }
        val grandTotal = totals.map { it.second }.sumOf { it }
        s += "Total: ${grandTotal.f()}"
        return s
    }

    fun totalsStringsThorough(): List<String> {
        val s = ArrayList<String>()
        for ((_, group) in groups) {
            s += "${group.groupName}:"
            s += "-".repeat(group.groupName.length + 1)
            s += group.logStringsWithTotal()
            s += "\n"
        }
        return s
    }
}

class SchwabCheckingGroup(val groupName: String, val rows: ArrayList<SchwabCheckingRow>) {
    val total get(): BigDecimal = computeTotal()
    fun computeTotal(): BigDecimal = rows.computeTotal()
    fun logStringsWithTotal(): List<String> = rows.cleanStringWithTotal()
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

fun List<SchwabCheckingRow>.computeTotal(): BigDecimal {
    val depositTotal = sumOf { it.deposit ?: zero }
    val withdrawalTotal = sumOf { it.withdrawal ?: zero }
    return depositTotal - withdrawalTotal
}
