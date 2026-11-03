package lox

import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

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

    private fun interpret(source: String): String {
        val output = ByteArrayOutputStream()
        val originalOut = System.out

        try {
            System.setOut(PrintStream(output))

            val tokens = Scanner(source).scanTokens()
            val statements = Parser(tokens).parse()

            val interpreter = Interpreter()
            val resolver = Resolver(interpreter)

            resolver.resolve(statements)
            interpreter.interpret(statements)
        } finally {
            System.setOut(originalOut)
        }

        return output.toString().trim()
    }

    private fun interpretError(source: String): String {
        val output = ByteArrayOutputStream()
        val originalError = System.err

        try {
            System.setErr(PrintStream(output))

            val tokens = Scanner(source).scanTokens()
            val statements = Parser(tokens).parse()

            val interpreter = Interpreter()
            val resolver = Resolver(interpreter)

            resolver.resolve(statements)
            interpreter.interpret(statements)
        } finally {
            System.setErr(originalError)
        }

        return output.toString().trim()
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
    fun evaluatesUnaryMinus() {
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
    }

    @Test
    fun evaluatesLogicalNot() {
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

        // nil is false
        assertEvaluates(
            true,
            Expr.Unary(
                Token(TokenType.BANG, "!", null, 1),
                Expr.Literal(null)
            )
        )

        // Everything else is true
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
    fun evaluatesSubtraction() {
        assertEvaluates(
            7.0,
            Expr.Binary(
                Expr.Literal(10.0),
                Token(TokenType.MINUS, "-", null, 1),
                Expr.Literal(3.0)
            )
        )

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
    fun evaluatesDivision() {
        assertEvaluates(
            5.0,
            Expr.Binary(
                Expr.Literal(10.0),
                Token(TokenType.SLASH, "/", null, 1),
                Expr.Literal(2.0)
            )
        )
    }

    @Test
    fun evaluatesMultiplication() {
        assertEvaluates(
            20.0,
            Expr.Binary(
                Expr.Literal(4.0),
                Token(TokenType.STAR, "*", null, 1),
                Expr.Literal(5.0)
            )
        )
    }

    @Test
    fun evaluatesAddition() {
        assertEvaluates(
            7.0,
            Expr.Binary(
                Expr.Literal(3.0),
                Token(TokenType.PLUS, "+", null, 1),
                Expr.Literal(4.0)
            )
        )

        assertEvaluates(
            4.0,
            Expr.Binary(
                Expr.Literal(2.5),
                Token(TokenType.PLUS, "+", null, 1),
                Expr.Literal(1.5)
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
    fun evaluatesGreaterThan() {
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
    }

    @Test
    fun evaluatesGreaterThanOrEqual() {
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
    }

    @Test
    fun evaluatesLessThan() {
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
    }

    @Test
    fun evaluatesLessThanOrEqual() {
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
    fun evaluatesEquality() {
        // Numbers
        assertEvaluates(
            true,
            Expr.Binary(
                Expr.Literal(5.0),
                Token(TokenType.EQUAL_EQUAL, "==", null, 1),
                Expr.Literal(5.0)
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

        // Different types
        assertEvaluates(
            false,
            Expr.Binary(
                Expr.Literal(5.0),
                Token(TokenType.EQUAL_EQUAL, "==", null, 1),
                Expr.Literal("5")
            )
        )
    }

    @Test
    fun evaluatesInequality() {
        // Numbers
        assertEvaluates(
            true,
            Expr.Binary(
                Expr.Literal(5.0),
                Token(TokenType.BANG_EQUAL, "!=", null, 1),
                Expr.Literal(3.0)
            )
        )

        // Booleans
        assertEvaluates(
            true,
            Expr.Binary(
                Expr.Literal(true),
                Token(TokenType.BANG_EQUAL, "!=", null, 1),
                Expr.Literal(false)
            )
        )

        // nil compared with another value
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
    fun evaluatesVariables() {
        val interpreter = Interpreter()
        val name = Token(TokenType.IDENTIFIER, "a", null, 1)

        interpreter.visitVarStmt(
            Stmt.Var(name, Expr.Literal(123.0))
        )

        assertEquals(
            123.0,
            Expr.Variable(name).accept(interpreter)
        )
    }

    @Test
    fun evaluatesAssignmentExpressions() {
        val interpreter = Interpreter()
        val name = Token(TokenType.IDENTIFIER, "a", null, 1)

        interpreter.visitVarStmt(
            Stmt.Var(name, Expr.Literal(123.0))
        )

        val assignment = Expr.Assign(name, Expr.Literal(456.0))

        assertEquals(456.0, assignment.accept(interpreter))
    }

    @Test
    fun assignmentsUpdateVariables() {
        val interpreter = Interpreter()
        val name = Token(TokenType.IDENTIFIER, "a", null, 1)

        interpreter.visitVarStmt(
            Stmt.Var(name, Expr.Literal(123.0))
        )

        Expr.Assign(name, Expr.Literal(456.0)).accept(interpreter)

        assertEquals(
            456.0,
            Expr.Variable(name).accept(interpreter)
        )
    }

    @Test
    fun evaluatesUninitializedVariables() {
        val interpreter = Interpreter()
        val name = Token(TokenType.IDENTIFIER, "a", null, 1)

        interpreter.visitVarStmt(
            Stmt.Var(name, null)
        )

        assertEquals(
            null,
            Expr.Variable(name).accept(interpreter)
        )
    }

    @Test
    fun accessesOuterVariablesInsideBlocks() {
        val interpreter = Interpreter()
        val name = Token(TokenType.IDENTIFIER, "a", null, 1)

        interpreter.visitVarStmt(
            Stmt.Var(name, Expr.Literal(123.0))
        )

        val outer = Environment()
        outer.define("a", 123.0)

        val inner = Environment(outer)

        val assignment = Expr.Assign(
            name,
            Expr.Literal(456.0)
        )

        interpreter.resolve(assignment, 1)

        interpreter.executeBlock(
            listOf(
                Stmt.Expression(assignment)
            ),
            inner
        )

        assertEquals(456.0, outer.get(name))
    }

    @Test
    fun restoresOuterEnvironmentAfterBlock() {
        val interpreter = Interpreter()
        val name = Token(TokenType.IDENTIFIER, "a", null, 1)

        interpreter.visitVarStmt(
            Stmt.Var(name, Expr.Literal(123.0))
        )

        interpreter.visitBlockStmt(
            Stmt.Block(
                listOf(
                    Stmt.Var(name, Expr.Literal(456.0))
                )
            )
        )

        assertEquals(
            123.0,
            Expr.Variable(name).accept(interpreter)
        )
    }

    @Test
    fun evaluatesLogicalOr() {
        assertEvaluates(
            true,
            Expr.Logical(
                Expr.Literal(true),
                Token(TokenType.OR, "or", null, 1),
                Expr.Literal(false)
            )
        )

        assertEvaluates(
            "right",
            Expr.Logical(
                Expr.Literal(false),
                Token(TokenType.OR, "or", null, 1),
                Expr.Literal("right")
            )
        )
    }

    @Test
    fun evaluatesLogicalAnd() {
        assertEvaluates(
            false,
            Expr.Logical(
                Expr.Literal(false),
                Token(TokenType.AND, "and", null, 1),
                Expr.Literal(true)
            )
        )

        assertEvaluates(
            "right",
            Expr.Logical(
                Expr.Literal(true),
                Token(TokenType.AND, "and", null, 1),
                Expr.Literal("right")
            )
        )
    }

    @Test
    fun logicalOrSkipsRightWhenLeftIsTrue() {
        val interpreter = Interpreter()
        val name = Token(TokenType.IDENTIFIER, "a", null, 1)

        interpreter.visitVarStmt(
            Stmt.Var(name, Expr.Literal(0.0))
        )

        Expr.Logical(
            Expr.Literal(true),
            Token(TokenType.OR, "or", null, 1),
            Expr.Assign(name, Expr.Literal(1.0))
        ).accept(interpreter)

        assertEquals(
            0.0,
            Expr.Variable(name).accept(interpreter)
        )
    }

    @Test
    fun logicalAndSkipsRightWhenLeftIsFalse() {
        val interpreter = Interpreter()
        val name = Token(TokenType.IDENTIFIER, "a", null, 1)

        interpreter.visitVarStmt(
            Stmt.Var(name, Expr.Literal(0.0))
        )

        Expr.Logical(
            Expr.Literal(false),
            Token(TokenType.AND, "and", null, 1),
            Expr.Assign(name, Expr.Literal(1.0))
        ).accept(interpreter)

        assertEquals(
            0.0,
            Expr.Variable(name).accept(interpreter)
        )
    }

    @Test
    fun ifExecutesThenBranchWhenConditionIsTrue() {
        val interpreter = Interpreter()
        val name = Token(TokenType.IDENTIFIER, "a", null, 1)

        interpreter.visitVarStmt(
            Stmt.Var(name, Expr.Literal(0.0))
        )

        interpreter.visitIfStmt(
            Stmt.If(
                Expr.Literal(true),
                Stmt.Expression(
                    Expr.Assign(name, Expr.Literal(1.0))
                ),
                null
            )
        )

        assertEquals(
            1.0,
            Expr.Variable(name).accept(interpreter)
        )
    }

    @Test
    fun ifExecutesElseBranchWhenConditionIsFalse() {
        val interpreter = Interpreter()
        val name = Token(TokenType.IDENTIFIER, "a", null, 1)

        interpreter.visitVarStmt(
            Stmt.Var(name, Expr.Literal(0.0))
        )

        interpreter.visitIfStmt(
            Stmt.If(
                Expr.Literal(false),
                Stmt.Expression(
                    Expr.Assign(name, Expr.Literal(1.0))
                ),
                Stmt.Expression(
                    Expr.Assign(name, Expr.Literal(2.0))
                )
            )
        )

        assertEquals(
            2.0,
            Expr.Variable(name).accept(interpreter)
        )
    }

    @Test
    fun ifDoesNothingWhenConditionIsFalseWithoutElse() {
        val interpreter = Interpreter()
        val name = Token(TokenType.IDENTIFIER, "a", null, 1)

        interpreter.visitVarStmt(
            Stmt.Var(name, Expr.Literal(0.0))
        )

        interpreter.visitIfStmt(
            Stmt.If(
                Expr.Literal(false),
                Stmt.Expression(
                    Expr.Assign(name, Expr.Literal(1.0))
                ),
                null
            )
        )

        assertEquals(
            0.0,
            Expr.Variable(name).accept(interpreter)
        )
    }

    @Test
    fun whileDoesNotExecuteWhenConditionIsFalse() {
        val interpreter = Interpreter()
        val name = Token(TokenType.IDENTIFIER, "a", null, 1)

        interpreter.visitVarStmt(
            Stmt.Var(name, Expr.Literal(0.0))
        )

        interpreter.visitWhileStmt(
            Stmt.While(
                Expr.Literal(false),
                Stmt.Expression(
                    Expr.Assign(name, Expr.Literal(1.0))
                )
            )
        )

        assertEquals(
            0.0,
            Expr.Variable(name).accept(interpreter)
        )
    }

    @Test
    fun whileRepeatsUntilConditionIsFalse() {
        val interpreter = Interpreter()
        val name = Token(TokenType.IDENTIFIER, "i", null, 1)

        interpreter.visitVarStmt(
            Stmt.Var(name, Expr.Literal(0.0))
        )

        interpreter.visitWhileStmt(
            Stmt.While(
                Expr.Binary(
                    Expr.Variable(name),
                    Token(TokenType.LESS, "<", null, 1),
                    Expr.Literal(3.0)
                ),
                Stmt.Expression(
                    Expr.Assign(
                        name,
                        Expr.Binary(
                            Expr.Variable(name),
                            Token(TokenType.PLUS, "+", null, 1),
                            Expr.Literal(1.0)
                        )
                    )
                )
            )
        )

        assertEquals(
            3.0,
            Expr.Variable(name).accept(interpreter)
        )
    }

    @Test
    fun callsNativeClockFunction() {
        val interpreter = Interpreter()

        val call = Expr.Call(
            Expr.Variable(
                Token(TokenType.IDENTIFIER, "clock", null, 1)
            ),
            Token(TokenType.RIGHT_PAREN, ")", null, 1),
            emptyList()
        )

        val result = call.accept(interpreter)

        kotlin.test.assertIs<Double>(result)
    }

    @Test
    fun functionDeclarationDefinesFunction() {
        val interpreter = Interpreter()
        val name = Token(TokenType.IDENTIFIER, "test", null, 1)

        interpreter.visitFunctionStmt(
            Stmt.Function(
                name,
                emptyList(),
                emptyList()
            )
        )

        val function = Expr.Variable(name).accept(interpreter)

        kotlin.test.assertIs<LoxFunction>(function)
    }

    @Test
    fun functionReceivesArguments() {
        val interpreter = Interpreter()

        val functionName = Token(
            TokenType.IDENTIFIER,
            "identity",
            null,
            1
        )

        val parameter = Token(
            TokenType.IDENTIFIER,
            "value",
            null,
            1
        )

        val variable = Expr.Variable(parameter)

        val function = Stmt.Function(
            functionName,
            listOf(parameter),
            listOf(
                Stmt.Return(
                    Token(TokenType.RETURN, "return", null, 1),
                    variable
                )
            )
        )

        val resolver = Resolver(interpreter)
        resolver.resolve(listOf(function))

        interpreter.visitFunctionStmt(function)

        val call = Expr.Call(
            Expr.Variable(functionName),
            Token(TokenType.RIGHT_PAREN, ")", null, 1),
            listOf(Expr.Literal(123.0))
        )

        assertEquals(123.0, call.accept(interpreter))
    }

    @Test
    fun functionWithoutReturnReturnsNil() {
        val interpreter = Interpreter()

        val name = Token(
            TokenType.IDENTIFIER,
            "test",
            null,
            1
        )

        interpreter.visitFunctionStmt(
            Stmt.Function(
                name,
                emptyList(),
                emptyList()
            )
        )

        val call = Expr.Call(
            Expr.Variable(name),
            Token(TokenType.RIGHT_PAREN, ")", null, 1),
            emptyList()
        )

        assertEquals(null, call.accept(interpreter))
    }

    @Test
    fun functionCapturesClosure() {
        val interpreter = Interpreter()

        val valueName = Token(
            TokenType.IDENTIFIER,
            "a",
            null,
            1
        )

        val functionName = Token(
            TokenType.IDENTIFIER,
            "getA",
            null,
            1
        )

        val variable = Expr.Variable(valueName)

        val functionDeclaration = Stmt.Function(
            functionName,
            emptyList(),
            listOf(
                Stmt.Return(
                    Token(TokenType.RETURN, "return", null, 1),
                    variable
                )
            )
        )

        val localEnvironment = Environment(interpreter.globals)
        localEnvironment.define("a", 123.0)

        interpreter.resolve(variable, 1)

        interpreter.executeBlock(
            listOf(functionDeclaration),
            localEnvironment
        )

        val function = localEnvironment.get(functionName)
        interpreter.globals.define("getA", function)

        val call = Expr.Call(
            Expr.Variable(functionName),
            Token(TokenType.RIGHT_PAREN, ")", null, 1),
            emptyList()
        )

        assertEquals(123.0, call.accept(interpreter))
    }

    @Test
    fun unaryMinusRejectsNonNumberOperand() {
        assertRuntimeError(
            "Operand must be a number.",
            Expr.Unary(
                Token(TokenType.MINUS, "-", null, 1),
                Expr.Literal("hello")
            )
        )
    }

    @Test
    fun subtractionRejectsNonNumberOperands() {
        assertRuntimeError(
            "Operands must be numbers.",
            Expr.Binary(
                Expr.Literal("hello"),
                Token(TokenType.MINUS, "-", null, 1),
                Expr.Literal(5.0)
            )
        )

        assertRuntimeError(
            "Operands must be numbers.",
            Expr.Binary(
                Expr.Literal(5.0),
                Token(TokenType.MINUS, "-", null, 1),
                Expr.Literal("hello")
            )
        )
    }

    @Test
    fun multiplicationRejectsNonNumberOperands() {
        assertRuntimeError(
            "Operands must be numbers.",
            Expr.Binary(
                Expr.Literal(5.0),
                Token(TokenType.STAR, "*", null, 1),
                Expr.Literal("hello")
            )
        )
    }

    @Test
    fun divisionRejectsNonNumberOperands() {
        assertRuntimeError(
            "Operands must be numbers.",
            Expr.Binary(
                Expr.Literal("hello"),
                Token(TokenType.SLASH, "/", null, 1),
                Expr.Literal(5.0)
            )
        )
    }

    @Test
    fun comparisonRejectsNonNumberOperands() {
        assertRuntimeError(
            "Operands must be numbers.",
            Expr.Binary(
                Expr.Literal("hello"),
                Token(TokenType.GREATER, ">", null, 1),
                Expr.Literal(5.0)
            )
        )
    }

    @Test
    fun additionRejectsMixedOperandTypes() {
        assertRuntimeError(
            "Operands must be two numbers or two strings.",
            Expr.Binary(
                Expr.Literal("hello"),
                Token(TokenType.PLUS, "+", null, 1),
                Expr.Literal(5.0)
            )
        )

        assertRuntimeError(
            "Operands must be two numbers or two strings.",
            Expr.Binary(
                Expr.Literal(5.0),
                Token(TokenType.PLUS, "+", null, 1),
                Expr.Literal("hello")
            )
        )
    }

    @Test
    fun rejectsCallingNonCallableValue() {
        val call = Expr.Call(
            Expr.Literal(123.0),
            Token(TokenType.RIGHT_PAREN, ")", null, 1),
            emptyList()
        )

        assertRuntimeError(
            "Can only call functions and classes.",
            call
        )
    }

    @Test
    fun rejectsIncorrectArgumentCount() {
        val interpreter = Interpreter()

        val function = object : LoxCallable {
            override fun arity() = 2

            override fun call(
                interpreter: Interpreter,
                arguments: List<Any?>
            ): Any? {
                return null
            }
        }

        interpreter.globals.define("test", function)

        val call = Expr.Call(
            Expr.Variable(
                Token(TokenType.IDENTIFIER, "test", null, 1)
            ),
            Token(TokenType.RIGHT_PAREN, ")", null, 1),
            listOf(Expr.Literal(1.0))
        )

        val error = assertFailsWith<RuntimeError> {
            call.accept(interpreter)
        }

        assertEquals(
            "Expected 2 arguments but got 1.",
            error.message
        )
    }

    @Test
    fun throwsRuntimeErrorForUndefinedVariable() {
        val interpreter = Interpreter()
        val name = Token(TokenType.IDENTIFIER, "a", null, 1)

        assertFailsWith<RuntimeError> {
            Expr.Variable(name).accept(interpreter)
        }
    }

    @Test
    fun throwsRuntimeErrorWhenAssigningUndefinedVariable() {
        val interpreter = Interpreter()
        val name = Token(TokenType.IDENTIFIER, "a", null, 1)

        assertFailsWith<RuntimeError> {
            Expr.Assign(name, Expr.Literal(123.0)).accept(interpreter)
        }
    }

    @Test
    fun classDeclarationDefinesClass() {
        val interpreter = Interpreter()
        val name = Token(TokenType.IDENTIFIER, "Breakfast", null, 1)

        interpreter.visitClassStmt(
            Stmt.Class(name, null, emptyList())
        )

        val klass = Expr.Variable(name).accept(interpreter)

        kotlin.test.assertIs<LoxClass>(klass)
        assertEquals("Breakfast", klass.toString())
    }

    @Test
    fun callingClassCreatesInstance() {
        val interpreter = Interpreter()
        val name = Token(TokenType.IDENTIFIER, "Breakfast", null, 1)

        interpreter.visitClassStmt(
            Stmt.Class(name, null, emptyList())
        )

        val call = Expr.Call(
            Expr.Variable(name),
            Token(TokenType.RIGHT_PAREN, ")", null, 1),
            emptyList()
        )

        val instance = call.accept(interpreter)

        kotlin.test.assertIs<LoxInstance>(instance)
        assertEquals("Breakfast instance", instance.toString())
    }

    @Test
    fun setsAndGetsInstanceField() {
        val interpreter = Interpreter()
        val klass = LoxClass("Breakfast", null, emptyMap())
        val instance = LoxInstance(klass)

        val field = Token(
            TokenType.IDENTIFIER,
            "meat",
            null,
            1
        )

        val set = Expr.Set(
            Expr.Literal(instance),
            field,
            Expr.Literal("sausage")
        )

        assertEquals("sausage", set.accept(interpreter))

        val get = Expr.Get(
            Expr.Literal(instance),
            field
        )

        assertEquals("sausage", get.accept(interpreter))
    }

    @Test
    fun rejectsGettingPropertyFromNonInstance() {
        val expression = Expr.Get(
            Expr.Literal("not an instance"),
            Token(TokenType.IDENTIFIER, "field", null, 1)
        )

        assertRuntimeError(
            "Only instances have properties.",
            expression
        )
    }

    @Test
    fun rejectsSettingFieldOnNonInstance() {
        val expression = Expr.Set(
            Expr.Literal("not an instance"),
            Token(TokenType.IDENTIFIER, "field", null, 1),
            Expr.Literal(123.0)
        )

        assertRuntimeError(
            "Only instances have fields.",
            expression
        )
    }

    @Test
    fun methodCanAccessThis() {
        val interpreter = Interpreter()

        val className = Token(
            TokenType.IDENTIFIER,
            "Breakfast",
            null,
            1
        )

        val thisExpression = Expr.This(
            Token(TokenType.THIS, "this", null, 1)
        )

        val method = Stmt.Function(
            Token(TokenType.IDENTIFIER, "getThis", null, 1),
            emptyList(),
            listOf(
                Stmt.Return(
                    Token(TokenType.RETURN, "return", null, 1),
                    thisExpression
                )
            )
        )

        val klass = Stmt.Class(
            className,
            null,
            listOf(method)
        )

        val resolver = Resolver(interpreter)
        resolver.resolve(listOf(klass))

        interpreter.visitClassStmt(klass)

        val instance = Expr.Call(
            Expr.Variable(className),
            Token(TokenType.RIGHT_PAREN, ")", null, 1),
            emptyList()
        ).accept(interpreter)

        val callMethod = Expr.Call(
            Expr.Get(
                Expr.Literal(instance),
                Token(TokenType.IDENTIFIER, "getThis", null, 1)
            ),
            Token(TokenType.RIGHT_PAREN, ")", null, 1),
            emptyList()
        )

        assertEquals(
            instance,
            callMethod.accept(interpreter)
        )
    }

    @Test
    fun methodCanAccessInstanceFieldThroughThis() {
        val interpreter = Interpreter()

        val className = Token(
            TokenType.IDENTIFIER,
            "Breakfast",
            null,
            1
        )

        val thisExpression = Expr.This(
            Token(TokenType.THIS, "this", null, 1)
        )

        val field = Token(
            TokenType.IDENTIFIER,
            "meat",
            null,
            1
        )

        val method = Stmt.Function(
            Token(TokenType.IDENTIFIER, "getMeat", null, 1),
            emptyList(),
            listOf(
                Stmt.Return(
                    Token(TokenType.RETURN, "return", null, 1),
                    Expr.Get(thisExpression, field)
                )
            )
        )

        val klass = Stmt.Class(
            className,
            null,
            listOf(method)
        )

        val resolver = Resolver(interpreter)
        resolver.resolve(listOf(klass))

        interpreter.visitClassStmt(klass)

        val instance = Expr.Call(
            Expr.Variable(className),
            Token(TokenType.RIGHT_PAREN, ")", null, 1),
            emptyList()
        ).accept(interpreter)

        kotlin.test.assertIs<LoxInstance>(instance)

        instance.set(field, "bacon")

        val callMethod = Expr.Call(
            Expr.Get(
                Expr.Literal(instance),
                Token(TokenType.IDENTIFIER, "getMeat", null, 1)
            ),
            Token(TokenType.RIGHT_PAREN, ")", null, 1),
            emptyList()
        )

        assertEquals(
            "bacon",
            callMethod.accept(interpreter)
        )
    }

    @Test
    fun initializerSetsInstanceField() {
        val interpreter = Interpreter()

        val className = Token(
            TokenType.IDENTIFIER,
            "Breakfast",
            null,
            1
        )

        val parameter = Token(
            TokenType.IDENTIFIER,
            "meat",
            null,
            1
        )

        val thisExpression = Expr.This(
            Token(TokenType.THIS, "this", null, 1)
        )

        val initializer = Stmt.Function(
            Token(TokenType.IDENTIFIER, "init", null, 1),
            listOf(parameter),
            listOf(
                Stmt.Expression(
                    Expr.Set(
                        thisExpression,
                        Token(TokenType.IDENTIFIER, "meat", null, 1),
                        Expr.Variable(parameter)
                    )
                )
            )
        )

        val klass = Stmt.Class(
            className,
            null,
            listOf(initializer)
        )

        val resolver = Resolver(interpreter)
        resolver.resolve(listOf(klass))

        interpreter.visitClassStmt(klass)

        val instance = Expr.Call(
            Expr.Variable(className),
            Token(TokenType.RIGHT_PAREN, ")", null, 1),
            listOf(Expr.Literal("bacon"))
        ).accept(interpreter)

        kotlin.test.assertIs<LoxInstance>(instance)

        assertEquals(
            "bacon",
            instance.get(
                Token(TokenType.IDENTIFIER, "meat", null, 1)
            )
        )
    }

    @Test
    fun subclassInheritsSuperclassMethod() {
        val output = interpret(
            """
        class Breakfast {
            serve() {
                print "breakfast";
            }
        }

        class Brunch < Breakfast {}

        Brunch().serve();
        """.trimIndent()
        )

        assertEquals("breakfast", output)
    }

    @Test
    fun subclassOverridesSuperclassMethod() {
        val output = interpret(
            """
        class Breakfast {
            serve() {
                print "breakfast";
            }
        }

        class Brunch < Breakfast {
            serve() {
                print "brunch";
            }
        }

        Brunch().serve();
        """.trimIndent()
        )

        assertEquals("brunch", output)
    }

    @Test
    fun superCallsSuperclassMethod() {
        val output = interpret(
            """
        class Breakfast {
            serve() {
                print "breakfast";
            }
        }

        class Brunch < Breakfast {
            serve() {
                super.serve();
                print "brunch";
            }
        }

        Brunch().serve();
        """.trimIndent()
        )

        assertEquals(
            """
        breakfast
        brunch
        """.trimIndent(),
            output
        )
    }

    @Test
    fun subclassInheritsInitializer() {
        val output = interpret(
            """
        class Breakfast {
            init(meat) {
                this.meat = meat;
            }
        }

        class Brunch < Breakfast {}

        var brunch = Brunch("bacon");
        print brunch.meat;
        """.trimIndent()
        )

        assertEquals("bacon", output)
    }

    @Test
    fun rejectsNonClassSuperclass() {
        val error = interpretError(
            """
        var Breakfast = "not a class";

        class Brunch < Breakfast {}
        """.trimIndent()
        )

        assertTrue(
            error.contains("Superclass must be a class.")
        )
    }

    @Test
    fun rejectsUndefinedSuperclassMethod() {
        val error = interpretError(
            """
        class Breakfast {}

        class Brunch < Breakfast {
            serve() {
                super.serve();
            }
        }

        Brunch().serve();
        """.trimIndent()
        )

        assertTrue(
            error.contains("Undefined property 'serve'.")
        )
    }

    @Test
    fun superMethodCanAccessThis() {
        val output = interpret(
            """
        class Parent {
            show() {
                print this.value;
            }
        }

        class Child < Parent {
            showParent() {
                super.show();
            }
        }

        var child = Child();
        child.value = "from child";
        child.showParent();
        """.trimIndent()
        )

        assertEquals("from child", output)
    }

    @Test
    fun inheritedMethodCanAccessThis() {
        val output = interpret(
            """
        class Parent {
            getValue() {
                print this.value;
            }
        }

        class Child < Parent {}

        var child = Child();
        child.value = "child value";
        child.getValue();
        """.trimIndent()
        )

        assertEquals("child value", output)
    }

    @Test
    fun subclassCanOverrideInheritedInitializer() {
        val output = interpret(
            """
        class Parent {
            init(value) {
                this.value = value;
            }
        }

        class Child < Parent {
            init() {
                this.value = "child";
            }
        }

        var child = Child();
        print child.value;
        """.trimIndent()
        )

        assertEquals("child", output)
    }

    @Test
    fun inheritedMethodUsesOverriddenSubclassMethod() {
        val output = interpret(
            """
        class Parent {
            method() {
                print "parent";
            }

            test() {
                this.method();
            }
        }

        class Child < Parent {
            method() {
                print "child";
            }
        }

        Child().test();
        """.trimIndent()
        )

        assertEquals("child", output)
    }

    @Test
    fun superBypassesSubclassOverride() {
        val output = interpret(
            """
        class Parent {
            method() {
                print "parent";
            }
        }

        class Child < Parent {
            method() {
                print "child";
            }

            test() {
                super.method();
            }
        }

        Child().test();
        """.trimIndent()
        )

        assertEquals("parent", output)
    }
}