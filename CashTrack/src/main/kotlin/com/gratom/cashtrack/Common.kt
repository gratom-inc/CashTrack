package com.gratom.cashtrack

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.LocalDate
import java.util.*

fun buildObjectMapper(): ObjectMapper = ObjectMapper()
    .registerModule(JavaTimeModule())
    .registerKotlinModule()

val objectMapper = buildObjectMapper()

interface TrDateRow {
    val trDate: LocalDate
}

fun <T : TrDateRow> ArrayList<out T>.filterFrom(cutoffDate: LocalDate): List<T> {
    return this.filter { it.trDate >= cutoffDate }
}

val numberFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.US)
fun BigDecimal.f(): String = numberFormat.format(this)
