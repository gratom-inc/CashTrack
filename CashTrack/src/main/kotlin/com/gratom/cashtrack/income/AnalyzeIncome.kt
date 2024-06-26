package com.gratom.cashtrack.income

// NOTE: https://github.com/FasterXML/jackson-module-kotlin/issues/437
import com.gratom.cashtrack.paychecksJsFilePath
import java.io.File
import javax.script.Compilable
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager


data class EmployerIncomeUS(
    val name: String = "",
    val start_offset: Int = 0,
    val final_offset: Int = 0,
    val paychecks: Map<String, PaycheckUS> = mapOf(),
    val projection: Boolean = false,
    val natural_order: Boolean = false
)

data class PaycheckUS(
    val gross: Double = 0.0,
    val net: Double = 0.0,
    val x401k: Double = 0.0,
    val hsa: Double = 0.0,
    val ftax: Double = 0.0,
    val sstax: Double = 0.0,
    val mtax: Double = 0.0,
    val stax: Double = 0.0,
    val ctax: Double = 0.0,
    val health: Double = 0.0,
    val metro: Double = 0.0,
    val other: Double = 0.0
)

data class HelloUser(
    val id: Int,
    val name: String,
    val email: String
)

val scriptEngineManager = ScriptEngineManager()
val javaScriptEngine: ScriptEngine = scriptEngineManager.getEngineByName("JavaScript")

fun analyzeIncome(): String {
    var s: String = "Income: \n"

    val paychecksJs = File(paychecksJsFilePath).readText()
    val script = (javaScriptEngine as Compilable).compile(paychecksJs)
    val scriptEvalResult = script.eval(javaScriptEngine.context)
    val paychecksJson: String = scriptEvalResult as String
    s += paychecksJson

//    val state = objectMapper.readValue<List<EmployerIncomeUS>>(paychecksJson)
//    s += "ReadJSON:\n" + state.joinToString("\n") + "\n"

    return s
}
