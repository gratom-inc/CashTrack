package com.gratom.cashtrack

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import java.io.File
import java.io.InputStreamReader

fun <RowType> readCsv(
    fileName: String,
    expectedColumnCount: Int,
    rowMaker: (List<String>) -> RowType,
    validator: (ArrayList<RowType>) -> Unit,
    csvFormat: CSVFormat = CSVFormat.DEFAULT,
    dropLines: Int = 1,
    reverse: Boolean = true
): ArrayList<RowType> {
    val reader: InputStreamReader = File(fileName).inputStream().reader()
    val parser: CSVParser = CSVFormat.Builder.create(csvFormat).apply {
        setIgnoreSurroundingSpaces(true)
    }.build().parse(reader)

    val rows = ArrayList(
        parser
            .drop(dropLines)
            .map { csvRecord ->
                val v = csvRecord.values().map { it.trim() }
                require(v.size == expectedColumnCount)
                rowMaker(v)
            }.let {
                if (reverse)
                    it.reversed()
                else
                    it
            }
    )
    validator(rows)
    return rows
}
