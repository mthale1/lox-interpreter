package lox

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertIs

class ParserTest {

    // Represents the expression structure that a parser test expects.
    // These classes mirror the possible Expr types so tests can describe an expected AST without using AstPrinter.
    private sealed class ExpectedExpr {
        data class Literal(val value: Any?) : ExpectedExpr()

        data class Unary(
            val operator: TokenType,
            val right: ExpectedExpr
        ) : ExpectedExpr()

        data class Binary(
            val left: ExpectedExpr,
            val operator: TokenType,
            val right: ExpectedExpr
        ) : ExpectedExpr()

        data class Logical(
            val left: ExpectedExpr,
            val operator: TokenType,
            val right: ExpectedExpr
        ) : ExpectedExpr()

        data class Grouping(
            val expression: ExpectedExpr
        ) : ExpectedExpr()

        data class Variable(
            val name: String
        ) : ExpectedExpr()

        data class Assign(
            val name: String,
            val value: ExpectedExpr
        ) : ExpectedExpr()

        data class Call(
            val callee: ExpectedExpr,
            val arguments: List<ExpectedExpr>
        ) : ExpectedExpr()
    }

    // Recursively compares the AST created by the parser to the expected AST.
    // Each expression type checks its own values and then checks any expressions nested inside of it.
    private fun assertExpression(actual: Expr?, expected: ExpectedExpr) {
        when (expected) {
            is ExpectedExpr.Literal -> {
                assertIs<Expr.Literal>(actual)
                assertEquals(expected.value, actual.value)
            }

            is ExpectedExpr.Unary -> {
                assertIs<Expr.Unary>(actual)
                assertEquals(expected.operator, actual.operator.type)
                assertExpression(actual.right, expected.right)
            }

            is ExpectedExpr.Binary -> {
                assertIs<Expr.Binary>(actual)
                assertEquals(expected.operator, actual.operator.type)
                assertExpression(actual.left, expected.left)
                assertExpression(actual.right, expected.right)
            }

            is ExpectedExpr.Logical -> {
                assertIs<Expr.Logical>(actual)
                assertEquals(expected.operator, actual.operator.type)
                assertExpression(actual.left, expected.left)
                assertExpression(actual.right, expected.right)
            }

            is ExpectedExpr.Grouping -> {
                assertIs<Expr.Grouping>(actual)
                assertExpression(actual.expression, expected.expression)
            }

            is ExpectedExpr.Variable -> {
                assertIs<Expr.Variable>(actual)
                assertEquals(expected.name, actual.name.lexeme)
            }

            is ExpectedExpr.Assign -> {
                assertIs<Expr.Assign>(actual)
                assertEquals(expected.name, actual.name.lexeme)
                assertExpression(actual.value, expected.value)
            }

            is ExpectedExpr.Call -> {
                assertIs<Expr.Call>(actual)
                assertExpression(actual.callee, expected.callee)
                assertEquals(expected.arguments.size, actual.arguments.size)

                for (i in expected.arguments.indices) {
                    assertExpression(actual.arguments[i], expected.arguments[i])
                }
            }
        }
    }

    private fun parse(source: String): Expr? {
        val tokens = Scanner("$source;").scanTokens()
        val statements = Parser(tokens).parse()

        if (statements.isEmpty()) return null

        val statement = statements[0] as? Stmt.Expression ?: return null
        return statement.expression
    }

    private fun parseStatements(source: String): List<Stmt> {
        val tokens = Scanner(source).scanTokens()
        return Parser(tokens).parse()
    }

    private fun assertLiteral(source: String, expected: Any?) {
        val expression = parse(source)

        assertIs<Expr.Literal>(expression)
        assertEquals(expected, expression.value)
    }

    private fun assertGrouping(source: String, expected: Any?) {
        val expression = parse(source)

        assertIs<Expr.Grouping>(expression)
        assertIs<Expr.Literal>(expression.expression)
        assertEquals(expected, expression.expression.value)
    }

    private fun assertUnary(
        source: String,
        expectedOperator: TokenType,
        expectedValue: Any?
    ) {
        val expression = parse(source)

        assertIs<Expr.Unary>(expression)
        assertEquals(expectedOperator, expression.operator.type)

        assertIs<Expr.Literal>(expression.right)
        assertEquals(expectedValue, expression.right.value)
    }

    private fun assertBinary(
        source: String,
        expectedOperator: TokenType,
        expectedLeft: Any?,
        expectedRight: Any?
    ) {
        val expression = parse(source)

        assertIs<Expr.Binary>(expression)
        assertEquals(expectedOperator, expression.operator.type)

        assertIs<Expr.Literal>(expression.left)
        assertEquals(expectedLeft, expression.left.value)

        assertIs<Expr.Literal>(expression.right)
        assertEquals(expectedRight, expression.right.value)
    }

    @Test
    fun parsesLiterals() {
        assertLiteral("123", 123.0)
        assertLiteral("45.67", 45.67)
        assertLiteral("\"hello\"", "hello")
        assertLiteral("true", true)
        assertLiteral("false", false)
        assertLiteral("nil", null)
    }

    @Test
    fun parsesGroupingExpressions() {
        assertGrouping("(123)", 123.0)
        assertGrouping("(45.67)", 45.67)
        assertGrouping("(\"hello\")", "hello")
        assertGrouping("(true)", true)
        assertGrouping("(false)", false)
        assertGrouping("(nil)", null)
    }

    @Test
    fun parsesUnaryExpressions() {
        assertUnary("-123", TokenType.MINUS, 123.0)
        assertUnary("-45.67", TokenType.MINUS, 45.67)
        assertUnary("!true", TokenType.BANG, true)
        assertUnary("!false", TokenType.BANG, false)
        assertUnary("!nil", TokenType.BANG, null)
    }

    @Test
    fun parsesNestedUnaryExpressions() {
        val doubleBang = ExpectedExpr.Unary(
            TokenType.BANG,
            ExpectedExpr.Unary(
                TokenType.BANG,
                ExpectedExpr.Literal(true)
            )
        )

        val doubleMinus = ExpectedExpr.Unary(
            TokenType.MINUS,
            ExpectedExpr.Unary(
                TokenType.MINUS,
                ExpectedExpr.Literal(123.0)
            )
        )

        assertExpression(parse("!!true"), doubleBang)
        assertExpression(parse("--123"), doubleMinus)
    }

    @Test
    fun parsesVariableExpression() {
        val expected = ExpectedExpr.Variable("a")

        assertExpression(parse("a"), expected)
    }

    @Test
    fun parsesAssignmentExpression() {
        val expected = ExpectedExpr.Assign(
            "a",
            ExpectedExpr.Literal(123.0)
        )

        assertExpression(parse("a = 123"), expected)
    }

    @Test
    fun parsesChainedAssignment() {
        val expected = ExpectedExpr.Assign(
            "a",
            ExpectedExpr.Assign(
                "b",
                ExpectedExpr.Literal(123.0)
            )
        )

        assertExpression(parse("a = b = 123"), expected)
    }

    @Test
    fun parsesExpressionStatement() {
        val statements = parseStatements("123;")

        assertEquals(1, statements.size)

        val statement = statements[0]
        assertIs<Stmt.Expression>(statement)

        assertExpression(
            statement.expression,
            ExpectedExpr.Literal(123.0)
        )
    }

    @Test
    fun parsesPrintStatement() {
        val statements = parseStatements("print 123;")

        assertEquals(1, statements.size)

        val statement = statements[0]
        assertIs<Stmt.Print>(statement)

        assertExpression(
            statement.expression,
            ExpectedExpr.Literal(123.0)
        )
    }

    @Test
    fun parsesVariableDeclaration() {
        val statements = parseStatements("var a = 123;")

        assertEquals(1, statements.size)

        val statement = statements[0]
        assertIs<Stmt.Var>(statement)

        assertEquals("a", statement.name.lexeme)

        assertExpression(
            statement.initializer,
            ExpectedExpr.Literal(123.0)
        )
    }

    @Test
    fun parsesVariableDeclarationWithoutInitializer() {
        val statements = parseStatements("var a;")

        assertEquals(1, statements.size)

        val statement = statements[0]
        assertIs<Stmt.Var>(statement)

        assertEquals("a", statement.name.lexeme)
        assertNull(statement.initializer)
    }

    @Test
    fun parsesBlockStatement() {
        val statements = parseStatements("{ print 123; }")

        assertEquals(1, statements.size)

        val statement = statements[0]
        assertIs<Stmt.Block>(statement)

        assertEquals(1, statement.statements.size)

        val innerStatement = statement.statements[0]
        assertIs<Stmt.Print>(innerStatement)

        assertExpression(
            innerStatement.expression,
            ExpectedExpr.Literal(123.0)
        )
    }

    @Test
    fun parsesNestedBlocks() {
        val statements = parseStatements("{ { print 123; } }")

        assertEquals(1, statements.size)

        val outerBlock = statements[0]
        assertIs<Stmt.Block>(outerBlock)

        assertEquals(1, outerBlock.statements.size)

        val innerBlock = outerBlock.statements[0]
        assertIs<Stmt.Block>(innerBlock)

        assertEquals(1, innerBlock.statements.size)

        val printStatement = innerBlock.statements[0]
        assertIs<Stmt.Print>(printStatement)

        assertExpression(
            printStatement.expression,
            ExpectedExpr.Literal(123.0)
        )
    }

    @Test
    fun parsesBlockWithMultipleStatements() {
        val statements = parseStatements("{ var a = 123; print a; }")

        assertEquals(1, statements.size)

        val block = statements[0]
        assertIs<Stmt.Block>(block)

        assertEquals(2, block.statements.size)

        val variable = block.statements[0]
        assertIs<Stmt.Var>(variable)
        assertEquals("a", variable.name.lexeme)
        assertExpression(
            variable.initializer,
            ExpectedExpr.Literal(123.0)
        )

        val print = block.statements[1]
        assertIs<Stmt.Print>(print)
        assertExpression(
            print.expression,
            ExpectedExpr.Variable("a")
        )
    }

    @Test
    fun parsesMultipleStatements() {
        val statements = parseStatements("var a = 123; print a;")

        assertEquals(2, statements.size)

        val variable = statements[0]
        assertIs<Stmt.Var>(variable)
        assertEquals("a", variable.name.lexeme)
        assertExpression(
            variable.initializer,
            ExpectedExpr.Literal(123.0)
        )

        val print = statements[1]
        assertIs<Stmt.Print>(print)
        assertExpression(
            print.expression,
            ExpectedExpr.Variable("a")
        )
    }

    @Test
    fun parsesAssignmentWithExpressionValue() {
        val expected = ExpectedExpr.Assign(
            "a",
            ExpectedExpr.Binary(
                ExpectedExpr.Literal(1.0),
                TokenType.PLUS,
                ExpectedExpr.Binary(
                    ExpectedExpr.Literal(2.0),
                    TokenType.STAR,
                    ExpectedExpr.Literal(3.0)
                )
            )
        )

        assertExpression(
            parse("a = 1 + 2 * 3"),
            expected
        )
    }

    @Test
    fun parsesAssignmentInsideBlock() {
        val statements = parseStatements("{ a = 123; }")

        assertEquals(1, statements.size)

        val block = statements[0]
        assertIs<Stmt.Block>(block)

        assertEquals(1, block.statements.size)

        val statement = block.statements[0]
        assertIs<Stmt.Expression>(statement)

        assertExpression(
            statement.expression,
            ExpectedExpr.Assign(
                "a",
                ExpectedExpr.Literal(123.0)
            )
        )
    }

    @Test
    fun parsesAssignmentToUninitializedVariable() {
        val statements = parseStatements("var a; a = 123;")

        assertEquals(2, statements.size)

        val variable = statements[0]
        assertIs<Stmt.Var>(variable)
        assertEquals("a", variable.name.lexeme)
        assertNull(variable.initializer)

        val assignment = statements[1]
        assertIs<Stmt.Expression>(assignment)

        assertExpression(
            assignment.expression,
            ExpectedExpr.Assign(
                "a",
                ExpectedExpr.Literal(123.0)
            )
        )
    }

    @Test
    fun parsesEmptyBlock() {
        val statements = parseStatements("{ }")

        assertEquals(1, statements.size)

        val block = statements[0]
        assertIs<Stmt.Block>(block)

        assertEquals(0, block.statements.size)
    }

    @Test
    fun parsesLogicalOrExpression() {
        val expected = ExpectedExpr.Logical(
            ExpectedExpr.Literal(true),
            TokenType.OR,
            ExpectedExpr.Literal(false)
        )

        assertExpression(
            parse("true or false"),
            expected
        )
    }

    @Test
    fun parsesLogicalAndExpression() {
        val expected = ExpectedExpr.Logical(
            ExpectedExpr.Literal(true),
            TokenType.AND,
            ExpectedExpr.Literal(false)
        )

        assertExpression(
            parse("true and false"),
            expected
        )
    }

    @Test
    fun parsesLogicalOperatorPrecedence() {
        val expected = ExpectedExpr.Logical(
            ExpectedExpr.Literal(true),
            TokenType.OR,
            ExpectedExpr.Logical(
                ExpectedExpr.Literal(false),
                TokenType.AND,
                ExpectedExpr.Literal(true)
            )
        )

        assertExpression(
            parse("true or false and true"),
            expected
        )
    }

    @Test
    fun parsesIfStatement() {
        val statements = parseStatements("if (true) print 123;")

        assertEquals(1, statements.size)

        val statement = statements[0]
        assertIs<Stmt.If>(statement)

        assertExpression(
            statement.condition,
            ExpectedExpr.Literal(true)
        )

        assertIs<Stmt.Print>(statement.thenBranch)
        assertExpression(
            statement.thenBranch.expression,
            ExpectedExpr.Literal(123.0)
        )

        assertNull(statement.elseBranch)
    }

    @Test
    fun parsesIfElseStatement() {
        val statements = parseStatements(
            "if (true) print 123; else print 456;"
        )

        assertEquals(1, statements.size)

        val statement = statements[0]
        assertIs<Stmt.If>(statement)

        assertExpression(
            statement.condition,
            ExpectedExpr.Literal(true)
        )

        assertIs<Stmt.Print>(statement.thenBranch)
        assertExpression(
            statement.thenBranch.expression,
            ExpectedExpr.Literal(123.0)
        )

        val elseBranch = statement.elseBranch
        assertIs<Stmt.Print>(elseBranch)
        assertExpression(
            elseBranch.expression,
            ExpectedExpr.Literal(456.0)
        )
    }

    @Test
    fun elseAttachesToNearestIf() {
        val statements = parseStatements(
            "if (true) if (false) print 1; else print 2;"
        )

        assertEquals(1, statements.size)

        val outerIf = statements[0]
        assertIs<Stmt.If>(outerIf)
        assertNull(outerIf.elseBranch)

        val innerIf = outerIf.thenBranch
        assertIs<Stmt.If>(innerIf)

        assertIs<Stmt.Print>(innerIf.thenBranch)

        val elseBranch = innerIf.elseBranch
        assertIs<Stmt.Print>(elseBranch)
    }

    @Test
    fun parsesWhileStatement() {
        val statements = parseStatements(
            "while (true) print 123;"
        )

        assertEquals(1, statements.size)

        val statement = statements[0]
        assertIs<Stmt.While>(statement)

        assertExpression(
            statement.condition,
            ExpectedExpr.Literal(true)
        )

        assertIs<Stmt.Print>(statement.body)
        assertExpression(
            statement.body.expression,
            ExpectedExpr.Literal(123.0)
        )
    }

    @Test
    fun parsesWhileWithExpressionCondition() {
        val statements = parseStatements(
            "while (i < 3) print i;"
        )

        assertEquals(1, statements.size)

        val statement = statements[0]
        assertIs<Stmt.While>(statement)

        assertExpression(
            statement.condition,
            ExpectedExpr.Binary(
                ExpectedExpr.Variable("i"),
                TokenType.LESS,
                ExpectedExpr.Literal(3.0)
            )
        )

        assertIs<Stmt.Print>(statement.body)
    }

    @Test
    fun parsesWhileWithBlockBody() {
        val statements = parseStatements(
            "while (true) { print 1; print 2; }"
        )

        assertEquals(1, statements.size)

        val statement = statements[0]
        assertIs<Stmt.While>(statement)

        val body = statement.body
        assertIs<Stmt.Block>(body)
        assertEquals(2, body.statements.size)

        assertIs<Stmt.Print>(body.statements[0])
        assertIs<Stmt.Print>(body.statements[1])
    }

    @Test
    fun parsesForStatement() {
        val statements = parseStatements(
            "for (var i = 0; i < 3; i = i + 1) print i;"
        )

        assertEquals(1, statements.size)

        val outerBlock = statements[0]
        assertIs<Stmt.Block>(outerBlock)
        assertEquals(2, outerBlock.statements.size)

        val initializer = outerBlock.statements[0]
        assertIs<Stmt.Var>(initializer)
        assertEquals("i", initializer.name.lexeme)
        assertExpression(
            initializer.initializer,
            ExpectedExpr.Literal(0.0)
        )

        val whileStatement = outerBlock.statements[1]
        assertIs<Stmt.While>(whileStatement)
    }

    @Test
    fun parsesForStatementWithExpressionInitializer() {
        val statements = parseStatements(
            "for (i = 0; i < 3; i = i + 1) print i;"
        )

        val outerBlock = statements[0]
        assertIs<Stmt.Block>(outerBlock)

        val initializer = outerBlock.statements[0]
        assertIs<Stmt.Expression>(initializer)

        assertExpression(
            initializer.expression,
            ExpectedExpr.Assign(
                "i",
                ExpectedExpr.Literal(0.0)
            )
        )
    }

    @Test
    fun forStatementUsesConditionForWhile() {
        val statements = parseStatements(
            "for (var i = 0; i < 3; i = i + 1) print i;"
        )

        val outerBlock = statements[0]
        assertIs<Stmt.Block>(outerBlock)

        val whileStatement = outerBlock.statements[1]
        assertIs<Stmt.While>(whileStatement)

        assertExpression(
            whileStatement.condition,
            ExpectedExpr.Binary(
                ExpectedExpr.Variable("i"),
                TokenType.LESS,
                ExpectedExpr.Literal(3.0)
            )
        )
    }

    @Test
    fun forStatementAddsIncrementAfterBody() {
        val statements = parseStatements(
            "for (var i = 0; i < 3; i = i + 1) print i;"
        )

        val outerBlock = statements[0]
        assertIs<Stmt.Block>(outerBlock)

        val whileStatement = outerBlock.statements[1]
        assertIs<Stmt.While>(whileStatement)

        val whileBody = whileStatement.body
        assertIs<Stmt.Block>(whileBody)
        assertEquals(2, whileBody.statements.size)

        val originalBody = whileBody.statements[0]
        assertIs<Stmt.Print>(originalBody)
        assertExpression(
            originalBody.expression,
            ExpectedExpr.Variable("i")
        )

        val increment = whileBody.statements[1]
        assertIs<Stmt.Expression>(increment)
        assertExpression(
            increment.expression,
            ExpectedExpr.Assign(
                "i",
                ExpectedExpr.Binary(
                    ExpectedExpr.Variable("i"),
                    TokenType.PLUS,
                    ExpectedExpr.Literal(1.0)
                )
            )
        )
    }

    @Test
    fun parsesForStatementWithoutInitializer() {
        val statements = parseStatements(
            "for (; true; false) print 123;"
        )

        assertEquals(1, statements.size)

        val statement = statements[0]
        assertIs<Stmt.While>(statement)
    }

    @Test
    fun forStatementWithoutConditionUsesTrue() {
        val statements = parseStatements(
            "for (var i = 0; ; i = i + 1) print i;"
        )

        val outerBlock = statements[0]
        assertIs<Stmt.Block>(outerBlock)

        val whileStatement = outerBlock.statements[1]
        assertIs<Stmt.While>(whileStatement)

        assertExpression(
            whileStatement.condition,
            ExpectedExpr.Literal(true)
        )
    }

    @Test
    fun parsesForStatementWithoutIncrement() {
        val statements = parseStatements(
            "for (var i = 0; i < 3;) print i;"
        )

        val outerBlock = statements[0]
        assertIs<Stmt.Block>(outerBlock)

        val whileStatement = outerBlock.statements[1]
        assertIs<Stmt.While>(whileStatement)

        assertIs<Stmt.Print>(whileStatement.body)
    }

    @Test
    fun parsesFactorExpressions() {
        assertBinary("2 * 3", TokenType.STAR, 2.0, 3.0)
        assertBinary("6 / 2", TokenType.SLASH, 6.0, 2.0)
        assertBinary("4.5 * 2", TokenType.STAR, 4.5, 2.0)
        assertBinary("10 / 5", TokenType.SLASH, 10.0, 5.0)
    }

    @Test
    fun parsesTermExpressions() {
        assertBinary("1 + 2", TokenType.PLUS, 1.0, 2.0)
        assertBinary("5 - 3", TokenType.MINUS, 5.0, 3.0)
        assertBinary("4.5 + 2", TokenType.PLUS, 4.5, 2.0)
        assertBinary("10 - 5", TokenType.MINUS, 10.0, 5.0)
    }

    @Test
    fun parsesComparisonExpressions() {
        assertBinary("1 < 2", TokenType.LESS, 1.0, 2.0)
        assertBinary("1 <= 2", TokenType.LESS_EQUAL, 1.0, 2.0)
        assertBinary("3 > 2", TokenType.GREATER, 3.0, 2.0)
        assertBinary("3 >= 2", TokenType.GREATER_EQUAL, 3.0, 2.0)
    }

    @Test
    fun parsesEqualityExpressions() {
        assertBinary("1 == 1", TokenType.EQUAL_EQUAL, 1.0, 1.0)
        assertBinary("1 != 2", TokenType.BANG_EQUAL, 1.0, 2.0)
        assertBinary("true == false", TokenType.EQUAL_EQUAL, true, false)
        assertBinary("nil != nil", TokenType.BANG_EQUAL, null, null)
    }

    @Test
    fun parsesOperatorPrecedence() {
        val multiplication = ExpectedExpr.Binary(
            ExpectedExpr.Literal(1.0),
            TokenType.PLUS,
            ExpectedExpr.Binary(
                ExpectedExpr.Literal(2.0),
                TokenType.STAR,
                ExpectedExpr.Literal(3.0)
            )
        )

        val addition = ExpectedExpr.Binary(
            ExpectedExpr.Binary(
                ExpectedExpr.Literal(1.0),
                TokenType.PLUS,
                ExpectedExpr.Literal(2.0)
            ),
            TokenType.GREATER,
            ExpectedExpr.Literal(3.0)
        )

        val comparison = ExpectedExpr.Binary(
            ExpectedExpr.Binary(
                ExpectedExpr.Literal(1.0),
                TokenType.LESS,
                ExpectedExpr.Literal(2.0)
            ),
            TokenType.EQUAL_EQUAL,
            ExpectedExpr.Literal(true)
        )

        assertExpression(parse("1 + 2 * 3"), multiplication)
        assertExpression(parse("1 + 2 > 3"), addition)
        assertExpression(parse("1 < 2 == true"), comparison)
    }

    @Test
    fun parsesOperatorsLeftToRight() {
        val subtraction = ExpectedExpr.Binary(
            ExpectedExpr.Binary(
                ExpectedExpr.Literal(10.0),
                TokenType.MINUS,
                ExpectedExpr.Literal(3.0)
            ),
            TokenType.MINUS,
            ExpectedExpr.Literal(2.0)
        )

        val division = ExpectedExpr.Binary(
            ExpectedExpr.Binary(
                ExpectedExpr.Literal(12.0),
                TokenType.SLASH,
                ExpectedExpr.Literal(3.0)
            ),
            TokenType.SLASH,
            ExpectedExpr.Literal(2.0)
        )

        val comparison = ExpectedExpr.Binary(
            ExpectedExpr.Binary(
                ExpectedExpr.Literal(1.0),
                TokenType.LESS,
                ExpectedExpr.Literal(2.0)
            ),
            TokenType.LESS,
            ExpectedExpr.Literal(3.0)
        )

        val equality = ExpectedExpr.Binary(
            ExpectedExpr.Binary(
                ExpectedExpr.Literal(1.0),
                TokenType.EQUAL_EQUAL,
                ExpectedExpr.Literal(2.0)
            ),
            TokenType.EQUAL_EQUAL,
            ExpectedExpr.Literal(3.0)
        )

        assertExpression(parse("10 - 3 - 2"), subtraction)
        assertExpression(parse("12 / 3 / 2"), division)
        assertExpression(parse("1 < 2 < 3"), comparison)
        assertExpression(parse("1 == 2 == 3"), equality)
    }

    @Test
    fun parsesGroupingPrecedence() {
        val leftGrouping = ExpectedExpr.Binary(
            ExpectedExpr.Grouping(
                ExpectedExpr.Binary(
                    ExpectedExpr.Literal(1.0),
                    TokenType.PLUS,
                    ExpectedExpr.Literal(2.0)
                )
            ),
            TokenType.STAR,
            ExpectedExpr.Literal(3.0)
        )

        val rightGrouping = ExpectedExpr.Binary(
            ExpectedExpr.Literal(1.0),
            TokenType.STAR,
            ExpectedExpr.Grouping(
                ExpectedExpr.Binary(
                    ExpectedExpr.Literal(2.0),
                    TokenType.PLUS,
                    ExpectedExpr.Literal(3.0)
                )
            )
        )

        val comparisonGrouping = ExpectedExpr.Binary(
            ExpectedExpr.Grouping(
                ExpectedExpr.Binary(
                    ExpectedExpr.Literal(1.0),
                    TokenType.PLUS,
                    ExpectedExpr.Literal(2.0)
                )
            ),
            TokenType.GREATER,
            ExpectedExpr.Literal(2.0)
        )

        assertExpression(parse("(1 + 2) * 3"), leftGrouping)
        assertExpression(parse("1 * (2 + 3)"), rightGrouping)
        assertExpression(parse("(1 + 2) > 2"), comparisonGrouping)
    }

    @Test
    fun parsesNestedExpressions() {
        val arithmetic = ExpectedExpr.Binary(
            ExpectedExpr.Unary(
                TokenType.MINUS,
                ExpectedExpr.Literal(123.0)
            ),
            TokenType.PLUS,
            ExpectedExpr.Grouping(
                ExpectedExpr.Binary(
                    ExpectedExpr.Literal(45.67),
                    TokenType.STAR,
                    ExpectedExpr.Literal(2.0)
                )
            )
        )

        val equality = ExpectedExpr.Binary(
            ExpectedExpr.Grouping(
                ExpectedExpr.Binary(
                    ExpectedExpr.Literal(1.0),
                    TokenType.PLUS,
                    ExpectedExpr.Literal(2.0)
                )
            ),
            TokenType.EQUAL_EQUAL,
            ExpectedExpr.Binary(
                ExpectedExpr.Literal(3.0),
                TokenType.STAR,
                ExpectedExpr.Literal(4.0)
            )
        )

        val unary = ExpectedExpr.Unary(
            TokenType.BANG,
            ExpectedExpr.Grouping(
                ExpectedExpr.Binary(
                    ExpectedExpr.Binary(
                        ExpectedExpr.Literal(1.0),
                        TokenType.PLUS,
                        ExpectedExpr.Literal(2.0)
                    ),
                    TokenType.GREATER,
                    ExpectedExpr.Literal(3.0)
                )
            )
        )

        val mixedPrecedence = ExpectedExpr.Binary(
            ExpectedExpr.Binary(
                ExpectedExpr.Literal(1.0),
                TokenType.PLUS,
                ExpectedExpr.Binary(
                    ExpectedExpr.Literal(2.0),
                    TokenType.STAR,
                    ExpectedExpr.Literal(3.0)
                )
            ),
            TokenType.EQUAL_EQUAL,
            ExpectedExpr.Literal(7.0)
        )

        val unaryAndComparison = ExpectedExpr.Binary(
            ExpectedExpr.Unary(
                TokenType.MINUS,
                ExpectedExpr.Literal(5.0)
            ),
            TokenType.LESS,
            ExpectedExpr.Binary(
                ExpectedExpr.Literal(2.0),
                TokenType.STAR,
                ExpectedExpr.Literal(3.0)
            )
        )

        assertExpression(parse("-123 + (45.67 * 2)"), arithmetic)
        assertExpression(parse("(1 + 2) == 3 * 4"), equality)
        assertExpression(parse("!(1 + 2 > 3)"), unary)
        assertExpression(parse("1 + 2 * 3 == 7"), mixedPrecedence)
        assertExpression(parse("-5 < 2 * 3"), unaryAndComparison)
    }

    @Test
    fun parsesFunctionCallWithoutArguments() {
        val expected = ExpectedExpr.Call(
            ExpectedExpr.Variable("clock"),
            emptyList()
        )

        assertExpression(parse("clock()"), expected)
    }

    @Test
    fun parsesFunctionCallWithArguments() {
        val expected = ExpectedExpr.Call(
            ExpectedExpr.Variable("add"),
            listOf(
                ExpectedExpr.Literal(1.0),
                ExpectedExpr.Literal(2.0)
            )
        )

        assertExpression(parse("add(1, 2)"), expected)
    }

    @Test
    fun parsesNestedFunctionCalls() {
        val expected = ExpectedExpr.Call(
            ExpectedExpr.Variable("outer"),
            listOf(
                ExpectedExpr.Call(
                    ExpectedExpr.Variable("inner"),
                    listOf(ExpectedExpr.Literal(1.0))
                )
            )
        )

        assertExpression(parse("outer(inner(1))"), expected)
    }

    @Test
    fun parsesFunctionDeclaration() {
        val statements = parseStatements(
            "fun add(a, b) { print a + b; }"
        )

        assertEquals(1, statements.size)

        val function = statements[0]
        assertIs<Stmt.Function>(function)

        assertEquals("add", function.name.lexeme)
        assertEquals(2, function.params.size)
        assertEquals("a", function.params[0].lexeme)
        assertEquals("b", function.params[1].lexeme)
        assertEquals(1, function.body.size)

        assertIs<Stmt.Print>(function.body[0])
    }

    @Test
    fun parsesFunctionWithoutParameters() {
        val statements = parseStatements(
            "fun test() { print 123; }"
        )

        assertEquals(1, statements.size)

        val function = statements[0]
        assertIs<Stmt.Function>(function)

        assertEquals("test", function.name.lexeme)
        assertEquals(0, function.params.size)
        assertEquals(1, function.body.size)
    }

    @Test
    fun parsesReturnWithValue() {
        val statements = parseStatements(
            "return 123;"
        )

        assertEquals(1, statements.size)

        val statement = statements[0]
        assertIs<Stmt.Return>(statement)

        assertEquals("return", statement.keyword.lexeme)
        assertExpression(
            statement.value,
            ExpectedExpr.Literal(123.0)
        )
    }

    @Test
    fun parsesReturnWithoutValue() {
        val statements = parseStatements(
            "return;"
        )

        assertEquals(1, statements.size)

        val statement = statements[0]
        assertIs<Stmt.Return>(statement)

        assertEquals("return", statement.keyword.lexeme)
        assertNull(statement.value)
    }

    @Test
    fun rejectsInvalidExpressions() {
        assertNull(parse(""))
        assertNull(parse("+"))
        assertNull(parse("1 +"))
        assertNull(parse("!"))
        assertNull(parse("-"))
        assertNull(parse("()"))
        assertNull(parse("1 *"))
        assertNull(parse("1 =="))
        assertNull(parse(")"))
    }

    @Test
    fun rejectsMissingClosingParenthesis() {
        assertNull(parse("(1 + 2"))
        assertNull(parse("((123)"))
        assertNull(parse("(1 * (2 + 3)"))
    }

    @Test
    fun rejectsInvalidAssignmentTarget() {
        val expression = parse("(a + b) = 123")

        assertIs<Expr.Grouping>(expression)
    }

    @Test
    fun rejectsMissingClosingBrace() {
        val statements = parseStatements("{ print 123;")

        assertEquals(0, statements.size)
    }

    @Test
    fun rejectsMissingSemicolon() {
        val statements = parseStatements("var a = 123")

        assertEquals(0, statements.size)
    }

    @Test
    fun rejectsMissingVariableName() {
        val statements = parseStatements("var = 123;")

        assertEquals(0, statements.size)
    }

    @Test
    fun rejectsMissingVariableInitializer() {
        val statements = parseStatements("var a =;")

        assertEquals(0, statements.size)
    }

    @Test
    fun rejectsMissingClosingParenthesisAfterIfCondition() {
        val statements = parseStatements(
            "if (true print 123;"
        )

        assertEquals(0, statements.size)
    }

    @Test
    fun rejectsMissingClosingParenthesisAfterWhileCondition() {
        val statements = parseStatements(
            "while (true print 123;"
        )

        assertEquals(0, statements.size)
    }

    @Test
    fun rejectsMissingClosingParenthesisAfterForClause() {
        val statements = parseStatements(
            "for (var i = 0; i < 3; i = i + 1 print i;"
        )

        assertEquals(0, statements.size)
    }

    @Test
    fun continuesParsingAfterInvalidDeclaration() {
        val statements = parseStatements("var = 123; print 456;")

        assertEquals(1, statements.size)

        val statement = statements[0]
        assertIs<Stmt.Print>(statement)

        assertExpression(
            statement.expression,
            ExpectedExpr.Literal(456.0)
        )
    }

}