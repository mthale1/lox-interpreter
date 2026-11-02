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

        interpreter.executeBlock(
            listOf(
                Stmt.Expression(
                    Expr.Assign(name, Expr.Literal(456.0))
                )
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

        interpreter.visitFunctionStmt(
            Stmt.Function(
                functionName,
                listOf(parameter),
                listOf(
                    Stmt.Return(
                        Token(TokenType.RETURN, "return", null, 1),
                        Expr.Variable(parameter)
                    )
                )
            )
        )

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

        val localEnvironment = Environment(interpreter.globals)
        localEnvironment.define("a", 123.0)

        interpreter.executeBlock(
            listOf(
                Stmt.Function(
                    functionName,
                    emptyList(),
                    listOf(
                        Stmt.Return(
                            Token(TokenType.RETURN, "return", null, 1),
                            Expr.Variable(valueName)
                        )
                    )
                )
            ),
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
    fun binaryOperatorsRejectNonNumberOperands() {
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
                Token(TokenType.STAR, "*", null, 1),
                Expr.Literal("hello")
            )
        )

        assertRuntimeError(
            "Operands must be numbers.",
            Expr.Binary(
                Expr.Literal("hello"),
                Token(TokenType.SLASH, "/", null, 1),
                Expr.Literal(5.0)
            )
        )

        assertRuntimeError(
            "Operands must be numbers.",
            Expr.Binary(
                Expr.Literal("hello"),
                Token(TokenType.GREATER, ">", null, 1),
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
}