package lox

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LoxIntegrationTest {

    @Test
    fun runsLoxFile() {
        val process = ProcessBuilder(
            "java",
            "-cp",
            System.getProperty("java.class.path"),
            "lox.LoxKt",
            "src/test/resources/expression.lox"
        ).start()

        val output = process.inputStream.bufferedReader().readText()
        val exitCode = process.waitFor()
        assertEquals(0, exitCode)
        assertEquals("(! (group (> (+ 1.0 2.0) 3.0)))", output.trim())
    }

    @Test
    fun exitsWithErrorForInvalidLoxFile() {
        val process = ProcessBuilder(
            "java",
            "-cp",
            System.getProperty("java.class.path"),
            "lox.LoxKt",
            "src/test/resources/invalid.lox"
        ).start()

        val exitCode = process.waitFor()

        assertEquals(65, exitCode)
    }

    @Test
    fun exitsWithUsageErrorForTooManyArguments() {
        val process = ProcessBuilder(
            "java",
            "-cp",
            System.getProperty("java.class.path"),
            "lox.LoxKt",
            "one.lox",
            "two.lox"
        ).start()

        val output = process.inputStream.bufferedReader().readText()
        val exitCode = process.waitFor()

        assertEquals(64, exitCode)
        assertEquals("Usage: klox [script]", output.trim())
    }

    @Test
    fun runsInteractivePrompt() {
        val process = ProcessBuilder(
            "java",
            "-cp",
            System.getProperty("java.class.path"),
            "lox.LoxKt"
        ).start()

        process.outputStream.bufferedWriter().use { writer ->
            writer.write("1 + 2 * 3\n")
        }

        val output = process.inputStream.bufferedReader().readText()
        val exitCode = process.waitFor()

        assertEquals(0, exitCode)
        assertTrue(output.contains("(+ 1.0 (* 2.0 3.0))"))
    }

    @Test
    fun interactivePromptContinuesAfterError() {
        val process = ProcessBuilder(
            "java",
            "-cp",
            System.getProperty("java.class.path"),
            "lox.LoxKt"
        ).start()

        process.outputStream.bufferedWriter().use { writer ->
            writer.write("1 +\n")
            writer.write("1 + 2\n")
        }

        val output = process.inputStream.bufferedReader().readText()
        val exitCode = process.waitFor()

        assertEquals(0, exitCode)
        assertTrue(output.contains("(+ 1.0 2.0)"))
    }
}