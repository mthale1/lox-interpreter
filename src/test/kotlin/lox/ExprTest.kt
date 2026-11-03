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
        override fun visitCallExpr(expr: Expr.Call) = "call"
        override fun visitVariableExpr(expr: Expr.Variable) = "variable"
        override fun visitAssignExpr(expr: Expr.Assign) = "assign"
        override fun visitLogicalExpr(expr: Expr.Logical) = "logical"
        override fun visitGetExpr(expr: Expr.Get) = "get"
        override fun visitSetExpr(expr: Expr.Set) = "set"
        override fun visitSuperExpr(expr: Expr.Super) = "super"
        override fun visitThisExpr(expr: Expr.This) = "this"
    }

    @Test
    fun literalStoresNumberValues() {
        assertEquals(123, Expr.Literal(123).value)
        assertEquals(0.75, Expr.Literal(0.75).value)
    }

    @Test
    fun literalStoresStringValue() {
        assertEquals(
            "string",
            Expr.Literal("string").value
        )
    }

    @Test
    fun literalStoresBooleanValues() {
        assertEquals(true, Expr.Literal(true).value)
        assertEquals(false, Expr.Literal(false).value)
    }

    @Test
    fun literalStoresNilValue() {
        assertEquals(null, Expr.Literal(null).value)
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
    fun variableStoresNames() {
        val names = listOf(
            Token(TokenType.IDENTIFIER, "a", null, 1),
            Token(TokenType.IDENTIFIER, "variable", null, 1),
            Token(TokenType.IDENTIFIER, "test123", null, 1)
        )

        for (name in names) {
            val expression = Expr.Variable(name)

            assertEquals(name, expression.name)
        }
    }

    @Test
    fun assignStoresNamesAndValues() {
        val name = Token(TokenType.IDENTIFIER, "a", null, 1)
        val variable = Expr.Variable(
            Token(TokenType.IDENTIFIER, "b", null, 1)
        )

        val expressions = listOf(
            Expr.Assign(name, literal),
            Expr.Assign(name, grouping),
            Expr.Assign(name, unary),
            Expr.Assign(name, binary),
            Expr.Assign(name, variable)
        )

        assertEquals(name, expressions[0].name)
        assertEquals(literal, expressions[0].value)

        assertEquals(name, expressions[1].name)
        assertEquals(grouping, expressions[1].value)

        assertEquals(name, expressions[2].name)
        assertEquals(unary, expressions[2].value)

        assertEquals(name, expressions[3].name)
        assertEquals(binary, expressions[3].value)

        assertEquals(name, expressions[4].name)
        assertEquals(variable, expressions[4].value)
    }

    @Test
    fun logicalStoresExpressionsAndOperator() {
        val operator = Token(TokenType.OR, "or", null, 1)
        val left = Expr.Literal(true)
        val right = Expr.Literal(false)

        val expression = Expr.Logical(left, operator, right)

        assertEquals(left, expression.left)
        assertEquals(operator, expression.operator)
        assertEquals(right, expression.right)
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

    @Test
    fun variableAcceptsVisitor() {
        val expression = Expr.Variable(
            Token(TokenType.IDENTIFIER, "a", null, 1)
        )

        assertEquals("variable", expression.accept(visitor))
    }

    @Test
    fun assignAcceptsVisitor() {
        val expression = Expr.Assign(
            Token(TokenType.IDENTIFIER, "a", null, 1),
            Expr.Literal(123)
        )

        assertEquals("assign", expression.accept(visitor))
    }

    @Test
    fun logicalAcceptsVisitor() {
        val expression = Expr.Logical(
            Expr.Literal(true),
            Token(TokenType.OR, "or", null, 1),
            Expr.Literal(false)
        )

        assertEquals("logical", expression.accept(visitor))
    }

    @Test
    fun callStoresCalleeParenAndArguments() {
        val callee = Expr.Variable(
            Token(TokenType.IDENTIFIER, "add", null, 1)
        )
        val paren = Token(TokenType.RIGHT_PAREN, ")", null, 1)
        val arguments = listOf(
            Expr.Literal(1.0),
            Expr.Literal(2.0)
        )

        val expression = Expr.Call(callee, paren, arguments)

        assertEquals(callee, expression.callee)
        assertEquals(paren, expression.paren)
        assertEquals(arguments, expression.arguments)
    }

    @Test
    fun callAcceptsVisitor() {
        val expression = Expr.Call(
            Expr.Variable(
                Token(TokenType.IDENTIFIER, "add", null, 1)
            ),
            Token(TokenType.RIGHT_PAREN, ")", null, 1),
            listOf(
                Expr.Literal(1.0),
                Expr.Literal(2.0)
            )
        )

        assertEquals("call", expression.accept(visitor))
    }

    @Test
    fun getStoresObjectAndName() {
        val obj = Expr.Variable(
            Token(TokenType.IDENTIFIER, "person", null, 1)
        )
        val name = Token(TokenType.IDENTIFIER, "name", null, 1)

        val expression = Expr.Get(obj, name)

        assertEquals(obj, expression.obj)
        assertEquals(name, expression.name)
    }

    @Test
    fun getAcceptsVisitor() {
        val expression = Expr.Get(
            Expr.Variable(
                Token(TokenType.IDENTIFIER, "person", null, 1)
            ),
            Token(TokenType.IDENTIFIER, "name", null, 1)
        )

        assertEquals("get", expression.accept(visitor))
    }

    @Test
    fun setStoresObjectNameAndValue() {
        val obj = Expr.Variable(
            Token(TokenType.IDENTIFIER, "person", null, 1)
        )
        val name = Token(TokenType.IDENTIFIER, "name", null, 1)
        val value = Expr.Literal("Makenzie")

        val expression = Expr.Set(obj, name, value)

        assertEquals(obj, expression.obj)
        assertEquals(name, expression.name)
        assertEquals(value, expression.value)
    }

    @Test
    fun setAcceptsVisitor() {
        val expression = Expr.Set(
            Expr.Variable(
                Token(TokenType.IDENTIFIER, "person", null, 1)
            ),
            Token(TokenType.IDENTIFIER, "name", null, 1),
            Expr.Literal("Makenzie")
        )

        assertEquals("set", expression.accept(visitor))
    }

    @Test
    fun thisStoresKeyword() {
        val keyword = Token(TokenType.THIS, "this", null, 1)

        val expression = Expr.This(keyword)

        assertEquals(keyword, expression.keyword)
    }

    @Test
    fun thisAcceptsVisitor() {
        val expression = Expr.This(
            Token(TokenType.THIS, "this", null, 1)
        )

        assertEquals("this", expression.accept(visitor))
    }

    @Test
    fun superStoresKeywordAndMethod() {
        val keyword = Token(
            TokenType.SUPER,
            "super",
            null,
            1
        )

        val method = Token(
            TokenType.IDENTIFIER,
            "method",
            null,
            1
        )

        val expression = Expr.Super(
            keyword,
            method
        )

        assertEquals(keyword, expression.keyword)
        assertEquals(method, expression.method)
    }

    @Test
    fun superAcceptsVisitor() {
        val expression = Expr.Super(
            Token(TokenType.SUPER, "super", null, 1),
            Token(TokenType.IDENTIFIER, "method", null, 1)
        )

        assertEquals(
            "super",
            expression.accept(visitor)
        )
    }

}