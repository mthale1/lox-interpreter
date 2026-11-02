package lox

import kotlin.test.Test
import kotlin.test.assertEquals

class AstPrinterTest {
    private val minus = Token(TokenType.MINUS, "-", null, 1)
    private val plus = Token(TokenType.PLUS, "+", null, 1)
    private val bang = Token(TokenType.BANG, "!", null, 1)

    private val literal = Expr.Literal(123)

    private val unary = Expr.Unary(
        minus,
        literal
    )

    private val binary = Expr.Binary(
        Expr.Literal(1),
        plus,
        Expr.Literal(2)
    )

    private val grouping = Expr.Grouping(
        literal
    )

    private fun assertPrint(expected: String, expression: Expr) {
        assertEquals(expected, AstPrinter().print(expression))
    }

    @Test
    fun printsLiterals() {
        assertPrint("123", Expr.Literal(123))           // integer
        assertPrint("0.75", Expr.Literal(0.75))         // decimal
        assertPrint("string", Expr.Literal("string"))   // string
        assertPrint("true", Expr.Literal(true))         // boolean
        assertPrint("false", Expr.Literal(false))
        assertPrint("nil", Expr.Literal(null))          // nil
    }

    @Test
    fun printsGroupingExpressions() {
        assertPrint("(group 123)", Expr.Grouping(literal))
        assertPrint("(group (group 123))", Expr.Grouping(grouping))
        assertPrint("(group (- 123))", Expr.Grouping(unary))
        assertPrint("(group (+ 1 2))", Expr.Grouping(binary))
    }

    @Test
    fun printsUnaryExpressions() {
        assertPrint("(- 123)", Expr.Unary(minus, literal))
        assertPrint("(! 123)", Expr.Unary(bang, literal))
        assertPrint("(- (group 123))", Expr.Unary(minus, grouping))
        assertPrint("(! (- 123))", Expr.Unary(bang, unary))
        assertPrint("(- (+ 1 2))", Expr.Unary(minus, binary))
    }

    @Test
    fun printsBinaryExpressions() {
        assertPrint("(+ 1 2)", binary)
        assertPrint("(+ 123 (group 123))", Expr.Binary(literal, plus, grouping))
        assertPrint("(+ (- 123) 123)", Expr.Binary(unary, plus, literal))
        assertPrint("(+ (+ 1 2) 123)", Expr.Binary(binary, plus, literal))
    }

    @Test
    fun printsBinaryOperators() {
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
            val expression = Expr.Binary(
                Expr.Literal(1),
                operator,
                Expr.Literal(2)
            )

            assertPrint("(${operator.lexeme} 1 2)", expression)
        }
    }

    @Test
    fun printsNestedExpression() {
        val expression = Expr.Binary(
            unary,
            plus,
            Expr.Grouping(binary)
        )

        assertPrint("(+ (- 123) (group (+ 1 2)))", expression)
    }

    @Test
    fun printsVariableExpressions() {
        val name = Token(TokenType.IDENTIFIER, "a", null, 1)

        assertPrint("a", Expr.Variable(name))
    }

    @Test
    fun printsAssignmentExpressions() {
        val name = Token(TokenType.IDENTIFIER, "a", null, 1)

        assertPrint("(= a 123)", Expr.Assign(name, Expr.Literal(123)))
    }

    @Test
    fun printsChainedAssignments() {
        val a = Token(TokenType.IDENTIFIER, "a", null, 1)
        val b = Token(TokenType.IDENTIFIER, "b", null, 1)

        val expression = Expr.Assign(
            a,
            Expr.Assign(
                b,
                Expr.Literal(123)
            )
        )

        assertPrint("(= a (= b 123))", expression)
    }

    @Test
    fun printsLogicalExpressions() {
        val or = Token(TokenType.OR, "or", null, 1)
        val and = Token(TokenType.AND, "and", null, 1)

        assertPrint(
            "(or true false)",
            Expr.Logical(
                Expr.Literal(true),
                or,
                Expr.Literal(false)
            )
        )

        assertPrint(
            "(and true false)",
            Expr.Logical(
                Expr.Literal(true),
                and,
                Expr.Literal(false)
            )
        )
    }

    @Test
    fun printsCallExpressions() {
        val callee = Expr.Variable(
            Token(TokenType.IDENTIFIER, "add", null, 1)
        )

        val expression = Expr.Call(
            callee,
            Token(TokenType.RIGHT_PAREN, ")", null, 1),
            listOf(
                Expr.Literal(1),
                Expr.Literal(2)
            )
        )

        assertPrint("(call add 1 2)", expression)
    }

}