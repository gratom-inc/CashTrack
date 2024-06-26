package com.gratom.cashtrack

import com.gratom.cashtrack.assets.schwab.parseDate

const val schwabCheckingConsolidatedCsvFilePath =
    "../../AllTrack/Assets/SCHC-Schwab-Checking/0-Schwab-Checking-Combined.csv"

val schwabCheckingCutoffDateA = parseDate("05/01/2022") // parseDate("10/23/2020")

const val paychecksJsonFilePath = "../../AllTrack/Code/Old/Ancient/Paychecks.json"
const val paychecksJsFilePath = "../../AllTrack/Code/Old/Ancient/Paychecks.js"
