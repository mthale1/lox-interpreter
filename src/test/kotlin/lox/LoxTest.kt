package lox

import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.Test
import kotlin.test.assertEquals

class LoxTest {

    private fun runExpression(source: String): String {
        val tokens = Scanner("$source;").scanTokens()
        val statements = Parser(tokens).parse()

        val statement = statements[0] as Stmt.Expression
        return AstPrinter().print(statement.expression)
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
            Lox.error(3, "Test error.")
        }

        assertError("[line 1] Error: Unexpected character.") {
            Lox.error(1, "Unexpected character.")
        }

        assertError("[line 25] Error: Invalid syntax.") {
            Lox.error(25, "Invalid syntax.")
        }
    }

    @Test
    fun reportsTokenError() {
        assertError("[line 5] Error at '+': Test error.") {
            Lox.error(Token(TokenType.PLUS, "+", null, 5), "Test error.")
        }

        assertError("[line 2] Error at '*': Unexpected token.") {
            Lox.error(Token(TokenType.STAR, "*", null, 2), "Unexpected token.")
        }

        assertError("[line 10] Error at '123': Invalid expression.") {
            Lox.error(Token(TokenType.NUMBER, "123", 123.0, 10), "Invalid expression.")
        }
    }

    @Test
    fun reportsEndOfFileError() {
        assertError("[line 7] Error at end: Expect expression.") {
            Lox.error(Token(TokenType.EOF, "", null, 7), "Expect expression.")
        }

        assertError("[line 1] Error at end: Unexpected end.") {
            Lox.error(Token(TokenType.EOF, "", null, 1), "Unexpected end.")
        }

        assertError("[line 15] Error at end: Missing expression.") {
            Lox.error(Token(TokenType.EOF, "", null, 15), "Missing expression.")
        }
    }

    @Test
    fun reportsRuntimeError() {
        assertError(
            "Operands must be numbers.\n[line 4]"
        ) {
            Lox.runtimeError(
                RuntimeError(
                    Token(TokenType.MINUS, "-", null, 4),
                    "Operands must be numbers."
                )
            )
        }

        assertError(
            "Operand must be a number.\n[line 7]"
        ) {
            Lox.runtimeError(
                RuntimeError(
                    Token(TokenType.MINUS, "-", null, 7),
                    "Operand must be a number."
                )
            )
        }

        assertError(
            "Operands must be two numbers or two strings.\n[line 12]"
        ) {
            Lox.runtimeError(
                RuntimeError(
                    Token(TokenType.PLUS, "+", null, 12),
                    "Operands must be two numbers or two strings."
                )
            )
        }
    }

    @Test
    fun runsCallExpressions() {
        assertEquals(
            "(call clock)",
            runExpression("clock()")
        )

        assertEquals(
            "(call add 1.0 2.0)",
            runExpression("add(1, 2)")
        )

        assertEquals(
            "(call outer (call inner 1.0))",
            runExpression("outer(inner(1))")
        )
    }

    @Test
    fun runsPropertyExpressions() {
        assertEquals(
            "(.meat breakfast)",
            runExpression("breakfast.meat")
        )

        assertEquals(
            "(=meat breakfast bacon)",
            runExpression("breakfast.meat = bacon")
        )

        assertEquals(
            "(.food (.plate breakfast))",
            runExpression("breakfast.plate.food")
        )
    }

    @Test
    fun runsThisExpressions() {
        assertEquals(
            "this",
            runExpression("this")
        )

        assertEquals(
            "(.meat this)",
            runExpression("this.meat")
        )
    }

    @Test
    fun runsMethodCallExpressions() {
        assertEquals(
            "(call (.serve breakfast))",
            runExpression("breakfast.serve()")
        )

        assertEquals(
            "(call (.serve breakfast) bacon eggs)",
            runExpression("breakfast.serve(bacon, eggs)")
        )
    }

    @Test
    fun runsSuperExpressions() {
        assertEquals(
            "(super.method)",
            runExpression("super.method")
        )

        assertEquals(
            "(call (super.method))",
            runExpression("super.method()")
        )
    }

}