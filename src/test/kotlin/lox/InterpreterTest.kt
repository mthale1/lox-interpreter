package lox

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class InterpreterTest {

    private fun assertEvaluates(expected: Any?, expression: Expr) {
        val interpreter = Interpreter()
        val result = expression.accept(interpreter)

        assertEquals(expected, result)
    }

    private fun assertRuntimeError(expectedMessage: String, expression: Expr) {
        val error = assertFailsWith<RuntimeError> {
            expression.accept(Interpreter())
        }

        assertEquals(expectedMessage, error.message)
    }

    @Test
    fun evaluatesLiterals() {
        assertEvaluates(42.0, Expr.Literal(42.0))
        assertEvaluates("hello", Expr.Literal("hello"))
        assertEvaluates(true, Expr.Literal(true))
        assertEvaluates(false, Expr.Literal(false))
        assertEvaluates(null, Expr.Literal(null))
    }

    @Test
    fun evaluatesGrouping() {
        assertEvaluates(
            42.0,
            Expr.Grouping(Expr.Literal(42.0))
        )

        assertEvaluates(
            "hello",
            Expr.Grouping(Expr.Literal("hello"))
        )

        assertEvaluates(
            true,
            Expr.Grouping(Expr.Literal(true))
        )

        assertEvaluates(
            null,
            Expr.Grouping(Expr.Literal(null))
        )

        assertEvaluates(
            3.0,
            Expr.Grouping(
                Expr.Binary(
                    Expr.Literal(1.0),
                    Token(TokenType.PLUS, "+", null, 1),
                    Expr.Literal(2.0)
                )
            )
        )
    }

    @Test
    fun evaluatesUnaryExpressions() {
        // Negation
        assertEvaluates(
            -5.0,
            Expr.Unary(
                Token(TokenType.MINUS, "-", null, 1),
                Expr.Literal(5.0)
            )
        )

        assertEvaluates(
            5.0,
            Expr.Unary(
                Token(TokenType.MINUS, "-", null, 1),
                Expr.Literal(-5.0)
            )
        )

        // Logical not
        assertEvaluates(
            false,
            Expr.Unary(
                Token(TokenType.BANG, "!", null, 1),
                Expr.Literal(true)
            )
        )

        assertEvaluates(
            true,
            Expr.Unary(
                Token(TokenType.BANG, "!", null, 1),
                Expr.Literal(false)
            )
        )

        // nil is falsey
        assertEvaluates(
            true,
            Expr.Unary(
                Token(TokenType.BANG, "!", null, 1),
                Expr.Literal(null)
            )
        )

        // Everything else is truthy
        assertEvaluates(
            false,
            Expr.Unary(
                Token(TokenType.BANG, "!", null, 1),
                Expr.Literal(0.0)
            )
        )

        assertEvaluates(
            false,
            Expr.Unary(
                Token(TokenType.BANG, "!", null, 1),
                Expr.Literal("")
            )
        )
    }

    @Test
    fun evaluatesArithmeticExpressions() {
        assertEvaluates(
            7.0,
            Expr.Binary(
                Expr.Literal(10.0),
                Token(TokenType.MINUS, "-", null, 1),
                Expr.Literal(3.0)
            )
        )

        assertEvaluates(
            5.0,
            Expr.Binary(
                Expr.Literal(10.0),
                Token(TokenType.SLASH, "/", null, 1),
                Expr.Literal(2.0)
            )
        )

        assertEvaluates(
            20.0,
            Expr.Binary(
                Expr.Literal(4.0),
                Token(TokenType.STAR, "*", null, 1),
                Expr.Literal(5.0)
            )
        )

        assertEvaluates(
            7.0,
            Expr.Binary(
                Expr.Literal(3.0),
                Token(TokenType.PLUS, "+", null, 1),
                Expr.Literal(4.0)
            )
        )

        // Decimal arithmetic
        assertEvaluates(
            4.0,
            Expr.Binary(
                Expr.Literal(2.5),
                Token(TokenType.PLUS, "+", null, 1),
                Expr.Literal(1.5)
            )
        )

        // Negative result
        assertEvaluates(
            -5.0,
            Expr.Binary(
                Expr.Literal(5.0),
                Token(TokenType.MINUS, "-", null, 1),
                Expr.Literal(10.0)
            )
        )
    }

    @Test
    fun concatenatesStrings() {
        assertEvaluates(
            "helloworld",
            Expr.Binary(
                Expr.Literal("hello"),
                Token(TokenType.PLUS, "+", null, 1),
                Expr.Literal("world")
            )
        )

        assertEvaluates(
            "hello world",
            Expr.Binary(
                Expr.Literal("hello "),
                Token(TokenType.PLUS, "+", null, 1),
                Expr.Literal("world")
            )
        )

        assertEvaluates(
            "",
            Expr.Binary(
                Expr.Literal(""),
                Token(TokenType.PLUS, "+", null, 1),
                Expr.Literal("")
            )
        )
    }

    @Test
    fun evaluatesComparisonExpressions() {
        // Greater than
        assertEvaluates(
            true,
            Expr.Binary(
                Expr.Literal(5.0),
                Token(TokenType.GREATER, ">", null, 1),
                Expr.Literal(3.0)
            )
        )

        assertEvaluates(
            false,
            Expr.Binary(
                Expr.Literal(3.0),
                Token(TokenType.GREATER, ">", null, 1),
                Expr.Literal(5.0)
            )
        )

        // Greater than or equal
        assertEvaluates(
            true,
            Expr.Binary(
                Expr.Literal(5.0),
                Token(TokenType.GREATER_EQUAL, ">=", null, 1),
                Expr.Literal(5.0)
            )
        )

        assertEvaluates(
            false,
            Expr.Binary(
                Expr.Literal(4.0),
                Token(TokenType.GREATER_EQUAL, ">=", null, 1),
                Expr.Literal(5.0)
            )
        )

        // Less than
        assertEvaluates(
            true,
            Expr.Binary(
                Expr.Literal(3.0),
                Token(TokenType.LESS, "<", null, 1),
                Expr.Literal(5.0)
            )
        )

        assertEvaluates(
            false,
            Expr.Binary(
                Expr.Literal(5.0),
                Token(TokenType.LESS, "<", null, 1),
                Expr.Literal(3.0)
            )
        )

        // Less than or equal
        assertEvaluates(
            true,
            Expr.Binary(
                Expr.Literal(5.0),
                Token(TokenType.LESS_EQUAL, "<=", null, 1),
                Expr.Literal(5.0)
            )
        )

        assertEvaluates(
            false,
            Expr.Binary(
                Expr.Literal(6.0),
                Token(TokenType.LESS_EQUAL, "<=", null, 1),
                Expr.Literal(5.0)
            )
        )
    }

    @Test
    fun evaluatesEqualityExpressions() {
        // Numbers
        assertEvaluates(
            true,
            Expr.Binary(
                Expr.Literal(5.0),
                Token(TokenType.EQUAL_EQUAL, "==", null, 1),
                Expr.Literal(5.0)
            )
        )

        assertEvaluates(
            true,
            Expr.Binary(
                Expr.Literal(5.0),
                Token(TokenType.BANG_EQUAL, "!=", null, 1),
                Expr.Literal(3.0)
            )
        )

        // Strings
        assertEvaluates(
            true,
            Expr.Binary(
                Expr.Literal("hello"),
                Token(TokenType.EQUAL_EQUAL, "==", null, 1),
                Expr.Literal("hello")
            )
        )

        assertEvaluates(
            false,
            Expr.Binary(
                Expr.Literal("hello"),
                Token(TokenType.EQUAL_EQUAL, "==", null, 1),
                Expr.Literal("world")
            )
        )

        // Booleans
        assertEvaluates(
            true,
            Expr.Binary(
                Expr.Literal(true),
                Token(TokenType.EQUAL_EQUAL, "==", null, 1),
                Expr.Literal(true)
            )
        )

        assertEvaluates(
            true,
            Expr.Binary(
                Expr.Literal(true),
                Token(TokenType.BANG_EQUAL, "!=", null, 1),
                Expr.Literal(false)
            )
        )

        // nil
        assertEvaluates(
            true,
            Expr.Binary(
                Expr.Literal(null),
                Token(TokenType.EQUAL_EQUAL, "==", null, 1),
                Expr.Literal(null)
            )
        )

        assertEvaluates(
            false,
            Expr.Binary(
                Expr.Literal(null),
                Token(TokenType.EQUAL_EQUAL, "==", null, 1),
                Expr.Literal(5.0)
            )
        )

        assertEvaluates(
            false,
            Expr.Binary(
                Expr.Literal(5.0),
                Token(TokenType.EQUAL_EQUAL, "==", null, 1),
                Expr.Literal("5")
            )
        )

        assertEvaluates(
            true,
            Expr.Binary(
                Expr.Literal(null),
                Token(TokenType.BANG_EQUAL, "!=", null, 1),
                Expr.Literal(false)
            )
        )
    }

    @Test
    fun evaluatesNestedExpressions() {
        // (1 + 2) * 3 = 9
        val addition = Expr.Binary(
            Expr.Literal(1.0),
            Token(TokenType.PLUS, "+", null, 1),
            Expr.Literal(2.0)
        )

        assertEvaluates(
            9.0,
            Expr.Binary(
                Expr.Grouping(addition),
                Token(TokenType.STAR, "*", null, 1),
                Expr.Literal(3.0)
            )
        )

        // -(2 + 3) = -5
        assertEvaluates(
            -5.0,
            Expr.Unary(
                Token(TokenType.MINUS, "-", null, 1),
                Expr.Grouping(
                    Expr.Binary(
                        Expr.Literal(2.0),
                        Token(TokenType.PLUS, "+", null, 1),
                        Expr.Literal(3.0)
                    )
                )
            )
        )

        // !(5 > 3) = false
        assertEvaluates(
            false,
            Expr.Unary(
                Token(TokenType.BANG, "!", null, 1),
                Expr.Binary(
                    Expr.Literal(5.0),
                    Token(TokenType.GREATER, ">", null, 1),
                    Expr.Literal(3.0)
                )
            )
        )
    }

    @Test
    fun throwsRuntimeErrorsForInvalidOperands() {
        // Unary minus requires a number
        assertRuntimeError(
            "Operand must be a number.",
            Expr.Unary(
                Token(TokenType.MINUS, "-", null, 1),
                Expr.Literal("hello")
            )
        )

        // Arithmetic operators require numbers
        assertRuntimeError(
            "Operands must be numbers.",
            Expr.Binary(
                Expr.Literal("hello"),
                Token(TokenType.MINUS, "-", null, 1),
                Expr.Literal(2.0)
            )
        )

        assertRuntimeError(
            "Operands must be numbers.",
            Expr.Binary(
                Expr.Literal(2.0),
                Token(TokenType.STAR, "*", null, 1),
                Expr.Literal("hello")
            )
        )

        assertRuntimeError(
            "Operands must be numbers.",
            Expr.Binary(
                Expr.Literal("hello"),
                Token(TokenType.SLASH, "/", null, 1),
                Expr.Literal(2.0)
            )
        )

        // Comparison operators require numbers
        assertRuntimeError(
            "Operands must be numbers.",
            Expr.Binary(
                Expr.Literal("hello"),
                Token(TokenType.GREATER, ">", null, 1),
                Expr.Literal(2.0)
            )
        )

        // + only allows two numbers or two strings
        assertRuntimeError(
            "Operands must be two numbers or two strings.",
            Expr.Binary(
                Expr.Literal("hello"),
                Token(TokenType.PLUS, "+", null, 1),
                Expr.Literal(2.0)
            )
        )

        assertRuntimeError(
            "Operands must be numbers.",
            Expr.Binary(
                Expr.Literal(2.0),
                Token(TokenType.MINUS, "-", null, 1),
                Expr.Literal("hello")
            )
        )

        assertRuntimeError(
            "Operands must be two numbers or two strings.",
            Expr.Binary(
                Expr.Literal(2.0),
                Token(TokenType.PLUS, "+", null, 1),
                Expr.Literal("hello")
            )
        )
    }

}