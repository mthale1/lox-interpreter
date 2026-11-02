package lox

import kotlin.test.Test
import kotlin.test.assertEquals

class StmtTest {

    private val visitor = object : Stmt.Visitor<String> {
        override fun visitBlockStmt(stmt: Stmt.Block) = "block"
        override fun visitExpressionStmt(stmt: Stmt.Expression) = "expression"
        override fun visitIfStmt(stmt: Stmt.If) = "if"
        override fun visitFunctionStmt(stmt: Stmt.Function) = "function"
        override fun visitPrintStmt(stmt: Stmt.Print) = "print"
        override fun visitReturnStmt(stmt: Stmt.Return) = "return"
        override fun visitVarStmt(stmt: Stmt.Var) = "var"
        override fun visitWhileStmt(stmt: Stmt.While) = "while"
    }

    @Test
    fun expressionStoresExpressions() {
        val expressions = listOf(
            Expr.Literal(123),
            Expr.Grouping(Expr.Literal(123)),
            Expr.Unary(
                Token(TokenType.MINUS, "-", null, 1),
                Expr.Literal(123)
            ),
            Expr.Binary(
                Expr.Literal(1),
                Token(TokenType.PLUS, "+", null, 1),
                Expr.Literal(2)
            )
        )

        for (expression in expressions) {
            val statement = Stmt.Expression(expression)

            assertEquals(expression, statement.expression)
        }
    }

    @Test
    fun printStoresExpressions() {
        val expressions = listOf(
            Expr.Literal(123),
            Expr.Grouping(Expr.Literal(123)),
            Expr.Unary(
                Token(TokenType.MINUS, "-", null, 1),
                Expr.Literal(123)
            ),
            Expr.Binary(
                Expr.Literal(1),
                Token(TokenType.PLUS, "+", null, 1),
                Expr.Literal(2)
            )
        )

        for (expression in expressions) {
            val statement = Stmt.Print(expression)

            assertEquals(expression, statement.expression)
        }
    }

    @Test
    fun varStoresNamesAndInitializers() {
        val name = Token(TokenType.IDENTIFIER, "a", null, 1)

        val initializers = listOf(
            Expr.Literal(123),
            Expr.Grouping(Expr.Literal(123)),
            Expr.Unary(
                Token(TokenType.MINUS, "-", null, 1),
                Expr.Literal(123)
            ),
            Expr.Binary(
                Expr.Literal(1),
                Token(TokenType.PLUS, "+", null, 1),
                Expr.Literal(2)
            )
        )

        for (initializer in initializers) {
            val statement = Stmt.Var(name, initializer)

            assertEquals(name, statement.name)
            assertEquals(initializer, statement.initializer)
        }
    }

    @Test
    fun varStoresNullInitializer() {
        val name = Token(TokenType.IDENTIFIER, "a", null, 1)
        val statement = Stmt.Var(name, null)

        assertEquals(name, statement.name)
        assertEquals(null, statement.initializer)
    }

    @Test
    fun blockStoresStatements() {
        val statements = listOf(
            Stmt.Expression(Expr.Literal(123)),
            Stmt.Print(Expr.Literal(456)),
            Stmt.Var(
                Token(TokenType.IDENTIFIER, "a", null, 1),
                Expr.Literal(789)
            )
        )

        val block = Stmt.Block(statements)

        assertEquals(statements, block.statements)
    }

    @Test
    fun blockStoresEmptyStatementList() {
        val statements = emptyList<Stmt>()

        val block = Stmt.Block(statements)

        assertEquals(statements, block.statements)
    }

    @Test
    fun ifStoresConditionAndBranches() {
        val condition = Expr.Literal(true)
        val thenBranch = Stmt.Print(Expr.Literal("yes"))
        val elseBranch = Stmt.Print(Expr.Literal("no"))

        val statement = Stmt.If(condition, thenBranch, elseBranch)

        assertEquals(condition, statement.condition)
        assertEquals(thenBranch, statement.thenBranch)
        assertEquals(elseBranch, statement.elseBranch)
    }

    @Test
    fun ifStoresNullElseBranch() {
        val condition = Expr.Literal(true)
        val thenBranch = Stmt.Print(Expr.Literal("yes"))

        val statement = Stmt.If(condition, thenBranch, null)

        assertEquals(condition, statement.condition)
        assertEquals(thenBranch, statement.thenBranch)
        assertEquals(null, statement.elseBranch)
    }

    @Test
    fun whileStoresConditionAndBody() {
        val condition = Expr.Literal(true)
        val body = Stmt.Print(Expr.Literal("loop"))

        val statement = Stmt.While(condition, body)

        assertEquals(condition, statement.condition)
        assertEquals(body, statement.body)
    }

    @Test
    fun functionStoresNameParametersAndBody() {
        val name = Token(TokenType.IDENTIFIER, "add", null, 1)
        val parameters = listOf(
            Token(TokenType.IDENTIFIER, "a", null, 1),
            Token(TokenType.IDENTIFIER, "b", null, 1)
        )
        val body = listOf<Stmt>(
            Stmt.Print(Expr.Variable(parameters[0]))
        )

        val statement = Stmt.Function(name, parameters, body)

        assertEquals(name, statement.name)
        assertEquals(parameters, statement.params)
        assertEquals(body, statement.body)
    }

    @Test
    fun returnStoresKeywordAndValue() {
        val keyword = Token(TokenType.RETURN, "return", null, 1)
        val value = Expr.Literal(123)

        val statement = Stmt.Return(keyword, value)

        assertEquals(keyword, statement.keyword)
        assertEquals(value, statement.value)
    }

    @Test
    fun returnStoresNullValue() {
        val keyword = Token(TokenType.RETURN, "return", null, 1)

        val statement = Stmt.Return(keyword, null)

        assertEquals(keyword, statement.keyword)
        assertEquals(null, statement.value)
    }

    @Test
    fun expressionAcceptsVisitor() {
        val statement = Stmt.Expression(
            Expr.Literal(123)
        )

        assertEquals("expression", statement.accept(visitor))
    }

    @Test
    fun printAcceptsVisitor() {
        val statement = Stmt.Print(
            Expr.Literal(123)
        )

        assertEquals("print", statement.accept(visitor))
    }

    @Test
    fun varAcceptsVisitor() {
        val statement = Stmt.Var(
            Token(TokenType.IDENTIFIER, "a", null, 1),
            Expr.Literal(123)
        )

        assertEquals("var", statement.accept(visitor))
    }

    @Test
    fun blockAcceptsVisitor() {
        val statement = Stmt.Block(
            listOf(Stmt.Print(Expr.Literal(123)))
        )

        assertEquals("block", statement.accept(visitor))
    }

    @Test
    fun ifAcceptsVisitor() {
        val statement = Stmt.If(
            Expr.Literal(true),
            Stmt.Print(Expr.Literal("yes")),
            null
        )

        assertEquals("if", statement.accept(visitor))
    }

    @Test
    fun whileAcceptsVisitor() {
        val statement = Stmt.While(
            Expr.Literal(true),
            Stmt.Print(Expr.Literal("loop"))
        )

        assertEquals("while", statement.accept(visitor))
    }

    @Test
    fun functionAcceptsVisitor() {
        val statement = Stmt.Function(
            Token(TokenType.IDENTIFIER, "test", null, 1),
            emptyList(),
            emptyList()
        )

        assertEquals("function", statement.accept(visitor))
    }

    @Test
    fun returnAcceptsVisitor() {
        val statement = Stmt.Return(
            Token(TokenType.RETURN, "return", null, 1),
            Expr.Literal(123)
        )

        assertEquals("return", statement.accept(visitor))
    }

}