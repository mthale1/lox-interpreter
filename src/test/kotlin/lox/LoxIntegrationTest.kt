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
            "lox.Lox",
            "src/test/resources/expression.lox"
        ).start()

        val output = process.inputStream.bufferedReader().readText()
        val exitCode = process.waitFor()
        assertEquals(0, exitCode)
        assertEquals("true", output.trim())
    }

    @Test
    fun exitsWithErrorForInvalidLoxFile() {
        val process = ProcessBuilder(
            "java",
            "-cp",
            System.getProperty("java.class.path"),
            "lox.Lox",
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
            "lox.Lox",
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
            "lox.Lox"
        ).start()

        process.outputStream.bufferedWriter().use { writer ->
            writer.write("1 + 2 * 3\n")
        }

        val output = process.inputStream.bufferedReader().readText()
        val exitCode = process.waitFor()

        assertEquals(0, exitCode)
        assertTrue(output.contains("7"))
    }

    @Test
    fun interactivePromptContinuesAfterError() {
        val process = ProcessBuilder(
            "java",
            "-cp",
            System.getProperty("java.class.path"),
            "lox.Lox"
        ).start()

        process.outputStream.bufferedWriter().use { writer ->
            writer.write("1 +\n")
            writer.write("1 + 2\n")
        }

        val output = process.inputStream.bufferedReader().readText()
        val exitCode = process.waitFor()

        assertEquals(0, exitCode)
        assertTrue(output.contains("3"))
    }

    @Test
    fun exitsWithRuntimeError() {
        val process = ProcessBuilder(
            "java",
            "-cp",
            System.getProperty("java.class.path"),
            "lox.Lox",
            "src/test/resources/runtime_error.lox"
        ).start()

        val errorOutput = process.errorStream.bufferedReader().readText()
        val exitCode = process.waitFor()

        assertEquals(70, exitCode)
        assertTrue(errorOutput.contains("Operands must be numbers."))
        assertTrue(errorOutput.contains("[line 1]"))
    }

    @Test
    fun stringifiesValuesCorrectly() {
        val process = ProcessBuilder(
            "java",
            "-cp",
            System.getProperty("java.class.path"),
            "lox.Lox"
        ).start()

        process.outputStream.bufferedWriter().use { writer ->
            writer.write("5.0\n")
            writer.write("5.25\n")
            writer.write("nil\n")
        }

        val output = process.inputStream.bufferedReader().readText()
        val exitCode = process.waitFor()

        assertEquals(0, exitCode)
        assertTrue(output.contains("5"))
        assertTrue(output.contains("5.25"))
        assertTrue(output.contains("nil"))
    }
}