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
            "src/test/resources/input/test_input.lox"
        ).start()

        val exitCode = process.waitFor()

        assertEquals(0, exitCode)
    }

    @Test
    fun loxFileProducesExpectedOutput() {
        val process = ProcessBuilder(
            "java",
            "-cp",
            System.getProperty("java.class.path"),
            "lox.Lox",
            "src/test/resources/input/test_input.lox"
        ).start()

        process.waitFor()

        val actualOutput = process.inputStream.bufferedReader().readText().trim()

        val expectedOutput = java.io.File(
            "src/test/resources/output/expected_test_output.txt"
        ).readText().trim()

        assertEquals(expectedOutput, actualOutput)
    }

    @Test
    fun exitsWithErrorForInvalidLoxFile() {
        val process = ProcessBuilder(
            "java",
            "-cp",
            System.getProperty("java.class.path"),
            "lox.Lox",
            "src/test/resources/input/invalid.lox"
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
            writer.write("print 1 + 2 * 3;\n")
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
            writer.write("print 1 +;\n")
            writer.write("print 1 + 2;\n")
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
            "src/test/resources/input/runtime_error.lox"
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
            writer.write("print 5.0;\n")
            writer.write("print 5.25;\n")
            writer.write("print nil;\n")
        }

        val output = process.inputStream.bufferedReader().readText()
        val exitCode = process.waitFor()

        assertEquals(0, exitCode)
        assertTrue(output.contains("5"))
        assertTrue(output.contains("5.25"))
        assertTrue(output.contains("nil"))
    }

    @Test
    fun runsVariableDeclarationsAndAssignments() {
        val process = ProcessBuilder(
            "java",
            "-cp",
            System.getProperty("java.class.path"),
            "lox.Lox",
            "src/test/resources/input/variables.lox"
        ).start()

        val output = process.inputStream.bufferedReader().readText()
        val exitCode = process.waitFor()

        assertEquals(0, exitCode)
        assertEquals("123\n456", output.trim())
    }

    @Test
    fun runsBlockScope() {
        val process = ProcessBuilder(
            "java",
            "-cp",
            System.getProperty("java.class.path"),
            "lox.Lox",
            "src/test/resources/input/block_scope.lox"
        ).start()

        val output = process.inputStream.bufferedReader().readText()
        val exitCode = process.waitFor()

        assertEquals(0, exitCode)
        assertEquals("block\nglobal", output.trim())
    }

    @Test
    fun exitsWithErrorForUndefinedVariable() {
        val process = ProcessBuilder(
            "java",
            "-cp",
            System.getProperty("java.class.path"),
            "lox.Lox",
            "src/test/resources/input/undefined_variable.lox"
        ).start()

        val errorOutput = process.errorStream.bufferedReader().readText()
        val exitCode = process.waitFor()

        assertEquals(70, exitCode)
        assertTrue(errorOutput.contains("Undefined variable 'a'."))
        assertTrue(errorOutput.contains("[line 1]"))
    }

    @Test
    fun runsIfElseStatements() {
        val process = ProcessBuilder(
            "java",
            "-cp",
            System.getProperty("java.class.path"),
            "lox.Lox",
            "src/test/resources/input/if_else.lox"
        ).start()

        val output = process.inputStream.bufferedReader().readText()
        val exitCode = process.waitFor()

        assertEquals(0, exitCode)
        assertEquals("then\nelse", output.trim())
    }

    @Test
    fun runsLogicalExpressions() {
        val process = ProcessBuilder(
            "java",
            "-cp",
            System.getProperty("java.class.path"),
            "lox.Lox",
            "src/test/resources/input/logical.lox"
        ).start()

        val output = process.inputStream.bufferedReader().readText()
        val exitCode = process.waitFor()

        assertEquals(0, exitCode)
        assertEquals(
            "left\nfallback\nfalse\nright",
            output.trim()
        )
    }

    @Test
    fun runsWhileLoops() {
        val process = ProcessBuilder(
            "java",
            "-cp",
            System.getProperty("java.class.path"),
            "lox.Lox",
            "src/test/resources/input/while.lox"
        ).start()

        val output = process.inputStream.bufferedReader().readText()
        val exitCode = process.waitFor()

        assertEquals(0, exitCode)
        assertEquals("0\n1\n2", output.trim())
    }

    @Test
    fun runsForLoops() {
        val process = ProcessBuilder(
            "java",
            "-cp",
            System.getProperty("java.class.path"),
            "lox.Lox",
            "src/test/resources/input/for.lox"
        ).start()

        val output = process.inputStream.bufferedReader().readText()
        val exitCode = process.waitFor()

        assertEquals(0, exitCode)
        assertEquals("0\n1\n2", output.trim())
    }

    @Test
    fun runsUserDefinedFunction() {
        val process = ProcessBuilder(
            "java",
            "-cp",
            System.getProperty("java.class.path"),
            "lox.Lox"
        ).start()

        process.outputStream.bufferedWriter().use { writer ->
            writer.write("fun add(a, b) { return a + b; }\n")
            writer.write("print add(2, 3);\n")
        }

        val output = process.inputStream.bufferedReader().readText()
        val exitCode = process.waitFor()

        assertEquals(0, exitCode)
        assertTrue(output.contains("5"))
    }

    @Test
    fun returnExitsFunctionEarly() {
        val process = ProcessBuilder(
            "java",
            "-cp",
            System.getProperty("java.class.path"),
            "lox.Lox"
        ).start()

        process.outputStream.bufferedWriter().use { writer ->
            writer.write("fun test() { return 123; print 456; }\n")
            writer.write("print test();\n")
        }

        val output = process.inputStream.bufferedReader().readText()
        val exitCode = process.waitFor()

        assertEquals(0, exitCode)
        assertTrue(output.contains("123"))
        assertTrue(!output.contains("456"))
    }

    @Test
    fun functionWithoutReturnProducesNil() {
        val process = ProcessBuilder(
            "java",
            "-cp",
            System.getProperty("java.class.path"),
            "lox.Lox"
        ).start()

        process.outputStream.bufferedWriter().use { writer ->
            writer.write("fun test() { }\n")
            writer.write("print test();\n")
        }

        val output = process.inputStream.bufferedReader().readText()
        val exitCode = process.waitFor()

        assertEquals(0, exitCode)
        assertTrue(output.contains("nil"))
    }

    @Test
    fun runsClosure() {
        val process = ProcessBuilder(
            "java",
            "-cp",
            System.getProperty("java.class.path"),
            "lox.Lox"
        ).start()

        process.outputStream.bufferedWriter().use { writer ->
            writer.write(
                "fun makeCounter() { " +
                        "var i = 0; " +
                        "fun count() { i = i + 1; print i; } " +
                        "return count; " +
                        "}\n"
            )
            writer.write("var counter = makeCounter();\n")
            writer.write("counter();\n")
            writer.write("counter();\n")
        }

        val output = process.inputStream.bufferedReader().readText()
        val exitCode = process.waitFor()

        assertEquals(0, exitCode)
        assertTrue(output.contains("1"))
        assertTrue(output.contains("2"))
    }

    @Test
    fun runsRecursiveFunction() {
        val process = ProcessBuilder(
            "java",
            "-cp",
            System.getProperty("java.class.path"),
            "lox.Lox"
        ).start()

        process.outputStream.bufferedWriter().use { writer ->
            writer.write(
                "fun fib(n) { " +
                        "if (n <= 1) return n; " +
                        "return fib(n - 2) + fib(n - 1); " +
                        "}\n"
            )
            writer.write("print fib(6);\n")
        }

        val output = process.inputStream.bufferedReader().readText()
        val exitCode = process.waitFor()

        assertEquals(0, exitCode)
        assertTrue(output.contains("8"))
    }

    @Test
    fun resolverPreservesLexicalScopeInClosure() {
        val process = ProcessBuilder(
            "java",
            "-cp",
            System.getProperty("java.class.path"),
            "lox.Lox",
            "src/test/resources/input/resolver_closure.lox"
        ).start()

        val output = process.inputStream.bufferedReader().readText()
        val exitCode = process.waitFor()

        assertEquals(0, exitCode)
        assertEquals(
            "global\nglobal",
            output.trim()
        )
    }

    @Test
    fun assignsToResolvedLocalVariable() {
        val process = ProcessBuilder(
            "java",
            "-cp",
            System.getProperty("java.class.path"),
            "lox.Lox"
        ).start()

        process.outputStream.bufferedWriter().use { writer ->
            writer.write("{ var a = 1; a = 2; print a; }\n")
        }

        val output = process.inputStream.bufferedReader().readText()
        val exitCode = process.waitFor()

        assertEquals(0, exitCode)
        assertTrue(output.contains("2"))
    }

    @Test
    fun exitsWithErrorForVariableInOwnInitializer() {
        val process = ProcessBuilder(
            "java",
            "-cp",
            System.getProperty("java.class.path"),
            "lox.Lox",
            "src/test/resources/input/own_initializer.lox"
        ).start()

        val errorOutput = process.errorStream.bufferedReader().readText()
        val exitCode = process.waitFor()

        assertEquals(65, exitCode)
        assertTrue(
            errorOutput.contains(
                "Can't read local variable in its own initializer."
            )
        )
    }

    @Test
    fun exitsWithErrorForDuplicateLocalVariable() {
        val process = ProcessBuilder(
            "java",
            "-cp",
            System.getProperty("java.class.path"),
            "lox.Lox",
            "src/test/resources/input/duplicate_local.lox"
        ).start()

        val errorOutput = process.errorStream.bufferedReader().readText()
        val exitCode = process.waitFor()

        assertEquals(65, exitCode)
        assertTrue(
            errorOutput.contains(
                "Already a variable with this name in this scope."
            )
        )
    }

    @Test
    fun exitsWithErrorForTopLevelReturn() {
        val process = ProcessBuilder(
            "java",
            "-cp",
            System.getProperty("java.class.path"),
            "lox.Lox",
            "src/test/resources/input/top_level_return.lox"
        ).start()

        val errorOutput = process.errorStream.bufferedReader().readText()
        val exitCode = process.waitFor()

        assertEquals(65, exitCode)
        assertTrue(
            errorOutput.contains(
                "Can't return from top-level code."
            )
        )
    }

    @Test
    fun runsClassesInstancesMethodsAndInitializers() {
        val process = ProcessBuilder(
            "java",
            "-cp",
            System.getProperty("java.class.path"),
            "lox.Lox",
            "src/test/resources/input/classes.lox"
        ).start()

        val output = process.inputStream.bufferedReader().readText()
        val exitCode = process.waitFor()

        assertEquals(0, exitCode)

        assertEquals(
            """
        Breakfast
        Breakfast instance
        bacon
        toast
        bacon and toast
        bacon and toast
        sausage
        """.trimIndent(),
            output.trim()
        )
    }

    @Test
    fun exitsWithErrorForThisOutsideClass() {
        val process = ProcessBuilder(
            "java",
            "-cp",
            System.getProperty("java.class.path"),
            "lox.Lox",
            "src/test/resources/input/this_outside_class.lox"
        ).start()

        val errorOutput = process.errorStream.bufferedReader().readText()
        val exitCode = process.waitFor()

        assertEquals(65, exitCode)
        assertTrue(
            errorOutput.contains(
                "Can't use 'this' outside of a class."
            )
        )
    }

    @Test
    fun exitsWithErrorForReturningValueFromInitializer() {
        val process = ProcessBuilder(
            "java",
            "-cp",
            System.getProperty("java.class.path"),
            "lox.Lox",
            "src/test/resources/input/initializer_return_value.lox"
        ).start()

        val errorOutput = process.errorStream.bufferedReader().readText()
        val exitCode = process.waitFor()

        assertEquals(65, exitCode)
        assertTrue(
            errorOutput.contains(
                "Can't return a value from an initializer."
            )
        )
    }

    @Test
    fun exitsWithErrorForUndefinedProperty() {
        val process = ProcessBuilder(
            "java",
            "-cp",
            System.getProperty("java.class.path"),
            "lox.Lox",
            "src/test/resources/input/undefined_property.lox"
        ).start()

        val errorOutput = process.errorStream.bufferedReader().readText()
        val exitCode = process.waitFor()

        assertEquals(70, exitCode)
        assertTrue(
            errorOutput.contains(
                "Undefined property 'meat'."
            )
        )
    }
}