package lox

import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.Test
import kotlin.test.assertEquals

class LoxTest {

    @Test
    fun reportsError() {
        val output = ByteArrayOutputStream()
        val originalError = System.err

        try {
            System.setErr(PrintStream(output))
            error(3, "Test error.")
        } finally {
            System.setErr(originalError)
        }

        assertEquals("[line 3] Error: Test error.", output.toString().trim())

    }
}