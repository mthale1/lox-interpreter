package lox

import kotlin.test.Test
import kotlin.test.assertEquals

class ExprTest {
    private val minus = Token(TokenType.MINUS, "-", null, 1)
    private val bang = Token(TokenType.BANG, "!", null, 1)
    private val plus = Token(TokenType.PLUS, "+", null, 1)

    private val literal = Expr.Literal(123)

    private val grouping = Expr.Grouping(
        Expr.Literal(123)
    )

    private val unary = Expr.Unary(
        minus,
        Expr.Literal(123)
    )

    private val binary = Expr.Binary(
        Expr.Literal(1),
        plus,
        Expr.Literal(2)
    )

    private val visitor = object : Expr.Visitor<String> {
        override fun visitLiteralExpr(expr: Expr.Literal) = "literal"
        override fun visitGroupingExpr(expr: Expr.Grouping) = "grouping"
        override fun visitUnaryExpr(expr: Expr.Unary) = "unary"
        override fun visitBinaryExpr(expr: Expr.Binary) = "binary"
    }

    @Test
    fun literalStoresValues() {
        val expressions = listOf(
            Expr.Literal(123).value,       // integer
            Expr.Literal(0.75).value,      // decimal
            Expr.Literal("string").value,  // string
            Expr.Literal(true).value,      // boolean
            Expr.Literal(null).value       // nil
        )

        assertEquals(123, expressions[0])
        assertEquals(0.75, expressions[1])
        assertEquals("string", expressions[2])
        assertEquals(true,expressions[3])
        assertEquals(null, expressions[4])
    }

    @Test
    fun groupingStoresExpressions() {
        val expressions = listOf(
            Expr.Grouping(literal),
            Expr.Grouping(grouping),
            Expr.Grouping(unary),
            Expr.Grouping(binary)
        )

        assertEquals(literal, expressions[0].expression)
        assertEquals(grouping, expressions[1].expression)
        assertEquals(unary, expressions[2].expression)
        assertEquals(binary, expressions[3].expression)
    }

    @Test
    fun unaryStoresOperatorsAndExpressions() {
        val expressions = listOf(
            Expr.Unary(minus, literal),
            Expr.Unary(bang, grouping),
            Expr.Unary(minus, unary),
            Expr.Unary(bang, binary)
        )

        assertEquals(minus, expressions[0].operator)
        assertEquals(literal, expressions[0].right)

        assertEquals(bang, expressions[1].operator)
        assertEquals(grouping, expressions[1].right)

        assertEquals(minus, expressions[2].operator)
        assertEquals(unary, expressions[2].right)

        assertEquals(bang, expressions[3].operator)
        assertEquals(binary, expressions[3].right)
    }

    @Test
    fun binaryStoresExpressionsAndOperator() {
        val expressions = listOf(
            Expr.Binary(literal, plus, grouping),
            Expr.Binary(grouping, plus, unary),
            Expr.Binary(unary, plus, binary),
            Expr.Binary(binary, plus, literal)
        )

        assertEquals(literal, expressions[0].left)
        assertEquals(plus, expressions[0].operator)
        assertEquals(grouping, expressions[0].right)

        assertEquals(grouping, expressions[1].left)
        assertEquals(plus, expressions[1].operator)
        assertEquals(unary, expressions[1].right)

        assertEquals(unary, expressions[2].left)
        assertEquals(plus, expressions[2].operator)
        assertEquals(binary, expressions[2].right)

        assertEquals(binary, expressions[3].left)
        assertEquals(plus, expressions[3].operator)
        assertEquals(literal, expressions[3].right)
    }

    @Test
    fun binaryStoresOperators() {
        val operators = listOf(
            Token(TokenType.PLUS, "+", null, 1),
            Token(TokenType.MINUS, "-", null, 1),
            Token(TokenType.STAR, "*", null, 1),
            Token(TokenType.SLASH, "/", null, 1),
            Token(TokenType.EQUAL_EQUAL, "==", null, 1),
            Token(TokenType.BANG_EQUAL, "!=", null, 1),
            Token(TokenType.GREATER, ">", null, 1),
            Token(TokenType.GREATER_EQUAL, ">=", null, 1),
            Token(TokenType.LESS, "<", null, 1),
            Token(TokenType.LESS_EQUAL, "<=", null, 1)
        )

        for (operator in operators) {
            val expression = Expr.Binary(literal, operator, literal)

            assertEquals(operator, expression.operator)
        }
    }

    @Test
    fun literalAcceptsVisitor() {
        val expression = Expr.Literal(1234)

        assertEquals("literal", expression.accept(visitor))
    }

    @Test
    fun groupingAcceptsVisitor() {
        val expression = Expr.Grouping(Expr.Literal(1234))

        assertEquals("grouping", expression.accept(visitor))
    }

    @Test
    fun unaryAcceptsVisitor() {
        val expression = Expr.Unary(
            Token(TokenType.MINUS, "-", null, 1),
            Expr.Literal(123)
        )

        assertEquals("unary", expression.accept(visitor))
    }

    @Test
    fun binaryAcceptsVisitor() {
        val expression = Expr.Binary(
            Expr.Literal(1),
            Token(TokenType.PLUS, "+", null, 1),
            Expr.Literal(2)
        )

        assertEquals("binary", expression.accept(visitor))
    }

}