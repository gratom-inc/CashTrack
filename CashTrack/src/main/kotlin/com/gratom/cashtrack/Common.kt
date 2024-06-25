package com.gratom.cashtrack

import java.math.BigDecimal
import java.text.NumberFormat
import java.time.LocalDate
import java.util.*

interface TrDateRow {
    val trDate: LocalDate
}

fun <T : TrDateRow> ArrayList<out T>.filterFrom(cutoffDate: LocalDate): List<T> {
    return this.filter { it.trDate >= cutoffDate }
}

val numberFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.US)
fun BigDecimal.f(): String = numberFormat.format(this)
