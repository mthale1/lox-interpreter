package lox

import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.Test
import kotlin.test.assertEquals

class LoxTest {

    private fun runExpression(source: String): String {
        val tokens = Scanner(source).scanTokens()
        val expression = Parser(tokens).parse()

        return AstPrinter().print(expression!!)
    }

    private fun captureError(action: () -> Unit): String {
        val output = ByteArrayOutputStream()
        val originalError = System.err

        try {
            System.setErr(PrintStream(output))
            action()
        } finally {
            System.setErr(originalError)
        }

        return output.toString().trim()
    }

    private fun assertError(expected: String, action: () -> Unit) {
        assertEquals(expected, captureError(action))
    }

    @Test
    fun runsLiteralExpressions() {
        assertEquals("123.0", runExpression("123"))
        assertEquals("45.67", runExpression("45.67"))
        assertEquals("hello", runExpression("\"hello\""))
        assertEquals("true", runExpression("true"))
        assertEquals("false", runExpression("false"))
        assertEquals("nil", runExpression("nil"))
    }

    @Test
    fun runsUnaryExpressions() {
        assertEquals("(- 123.0)", runExpression("-123"))
        assertEquals("(! true)", runExpression("!true"))
        assertEquals("(! false)", runExpression("!false"))
        assertEquals("(! nil)", runExpression("!nil"))
        assertEquals("(! (! true))", runExpression("!!true"))
        assertEquals("(- (- 123.0))", runExpression("--123"))
    }

    @Test
    fun runsBinaryExpressions() {
        assertEquals("(+ 1.0 2.0)", runExpression("1 + 2"))
        assertEquals("(- 5.0 3.0)", runExpression("5 - 3"))
        assertEquals("(* 2.0 3.0)", runExpression("2 * 3"))
        assertEquals("(/ 6.0 2.0)", runExpression("6 / 2"))

        assertEquals("(< 1.0 2.0)", runExpression("1 < 2"))
        assertEquals("(<= 1.0 2.0)", runExpression("1 <= 2"))
        assertEquals("(> 3.0 2.0)", runExpression("3 > 2"))
        assertEquals("(>= 3.0 2.0)", runExpression("3 >= 2"))

        assertEquals("(== 1.0 1.0)", runExpression("1 == 1"))
        assertEquals("(!= 1.0 2.0)", runExpression("1 != 2"))
    }

    @Test
    fun runsGroupedExpressions() {
        assertEquals(
            "(* (group (+ 1.0 2.0)) 3.0)",
            runExpression("(1 + 2) * 3")
        )

        assertEquals(
            "(* 1.0 (group (+ 2.0 3.0)))",
            runExpression("1 * (2 + 3)")
        )

        assertEquals(
            "(> (group (+ 1.0 2.0)) 2.0)",
            runExpression("(1 + 2) > 2")
        )
    }

    @Test
    fun runsExpressionsWithCorrectPrecedence() {
        assertEquals(
            "(+ 1.0 (* 2.0 3.0))",
            runExpression("1 + 2 * 3")
        )

        assertEquals(
            "(> (+ 1.0 2.0) 2.0)",
            runExpression("1 + 2 > 2")
        )

        assertEquals(
            "(== (< 1.0 2.0) true)",
            runExpression("1 < 2 == true")
        )

        assertEquals(
            "(== (+ 1.0 (* 2.0 3.0)) 7.0)",
            runExpression("1 + 2 * 3 == 7")
        )
    }

    @Test
    fun runsOperatorsLeftToRight() {
        assertEquals(
            "(- (- 10.0 3.0) 2.0)",
            runExpression("10 - 3 - 2")
        )

        assertEquals(
            "(/ (/ 12.0 3.0) 2.0)",
            runExpression("12 / 3 / 2")
        )

        assertEquals(
            "(< (< 1.0 2.0) 3.0)",
            runExpression("1 < 2 < 3")
        )

        assertEquals(
            "(== (== 1.0 2.0) 3.0)",
            runExpression("1 == 2 == 3")
        )
    }

    @Test
    fun runsNestedExpressions() {
        assertEquals(
            "(+ (- 123.0) (group (* 45.67 2.0)))",
            runExpression("-123 + (45.67 * 2)")
        )

        assertEquals(
            "(== (group (+ 1.0 2.0)) (* 3.0 4.0))",
            runExpression("(1 + 2) == 3 * 4")
        )

        assertEquals(
            "(! (group (> (+ 1.0 2.0) 3.0)))",
            runExpression("!(1 + 2 > 3)")
        )

        assertEquals(
            "(< (- 5.0) (* 2.0 3.0))",
            runExpression("-5 < 2 * 3")
        )
    }

    @Test
    fun reportsLineError() {
        assertError("[line 3] Error: Test error.") {
            error(3, "Test error.")
        }

        assertError("[line 1] Error: Unexpected character.") {
            error(1, "Unexpected character.")
        }

        assertError("[line 25] Error: Invalid syntax.") {
            error(25, "Invalid syntax.")
        }
    }

    @Test
    fun reportsTokenError() {
        assertError("[line 5] Error at '+': Test error.") {
            error(Token(TokenType.PLUS, "+", null, 5), "Test error.")
        }

        assertError("[line 2] Error at '*': Unexpected token.") {
            error(Token(TokenType.STAR, "*", null, 2), "Unexpected token.")
        }

        assertError("[line 10] Error at '123': Invalid expression.") {
            error(Token(TokenType.NUMBER, "123", 123.0, 10), "Invalid expression.")
        }
    }

    @Test
    fun reportsEndOfFileError() {
        assertError("[line 7] Error at end: Expect expression.") {
            error(Token(TokenType.EOF, "", null, 7), "Expect expression.")
        }

        assertError("[line 1] Error at end: Unexpected end.") {
            error(Token(TokenType.EOF, "", null, 1), "Unexpected end.")
        }

        assertError("[line 15] Error at end: Missing expression.") {
            error(Token(TokenType.EOF, "", null, 15), "Missing expression.")
        }
    }

}