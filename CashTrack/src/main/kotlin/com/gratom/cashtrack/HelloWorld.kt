package com.gratom.cashtrack

class HelloWorld {
    val desc: String = "Name"
    val amt: Double = 100.0
}

val hw = HelloWorld()

fun foo(): String {
    val jsonString: String = objectMapper.writeValueAsString(hw)
    return jsonString
}
