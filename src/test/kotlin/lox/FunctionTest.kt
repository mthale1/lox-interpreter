package lox

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FunctionTest {

    @Test
    fun arityMatchesParameterCount() {
        val declaration = Stmt.Function(
            Token(TokenType.IDENTIFIER, "add", null, 1),
            listOf(
                Token(TokenType.IDENTIFIER, "a", null, 1),
                Token(TokenType.IDENTIFIER, "b", null, 1)
            ),
            emptyList()
        )

        val function = LoxFunction(
            declaration,
            Environment()
        )

        assertEquals(2, function.arity())
    }

    @Test
    fun callReturnsFunctionReturnValue() {
        val parameter = Token(
            TokenType.IDENTIFIER,
            "value",
            null,
            1
        )

        val declaration = Stmt.Function(
            Token(TokenType.IDENTIFIER, "identity", null, 1),
            listOf(parameter),
            listOf(
                Stmt.Return(
                    Token(TokenType.RETURN, "return", null, 1),
                    Expr.Variable(parameter)
                )
            )
        )

        val function = LoxFunction(
            declaration,
            Environment()
        )

        assertEquals(
            123.0,
            function.call(
                Interpreter(),
                listOf(123.0)
            )
        )
    }

    @Test
    fun callReturnsNilWithoutReturnStatement() {
        val declaration = Stmt.Function(
            Token(TokenType.IDENTIFIER, "test", null, 1),
            emptyList(),
            emptyList()
        )

        val function = LoxFunction(
            declaration,
            Environment()
        )

        assertEquals(
            null,
            function.call(
                Interpreter(),
                emptyList()
            )
        )
    }

    @Test
    fun functionUsesClosure() {
        val closure = Environment()
        closure.define("a", 123.0)

        val name = Token(
            TokenType.IDENTIFIER,
            "a",
            null,
            1
        )

        val declaration = Stmt.Function(
            Token(TokenType.IDENTIFIER, "getA", null, 1),
            emptyList(),
            listOf(
                Stmt.Return(
                    Token(TokenType.RETURN, "return", null, 1),
                    Expr.Variable(name)
                )
            )
        )

        val function = LoxFunction(
            declaration,
            closure
        )

        assertEquals(
            123.0,
            function.call(
                Interpreter(),
                emptyList()
            )
        )
    }

    @Test
    fun stringRepresentationUsesFunctionName() {
        val declaration = Stmt.Function(
            Token(TokenType.IDENTIFIER, "add", null, 1),
            emptyList(),
            emptyList()
        )

        val function = LoxFunction(
            declaration,
            Environment()
        )

        assertEquals("<fn add>", function.toString())
    }

    @Test
    fun callableProvidesArityAndCall() {
        val callable = object : LoxCallable {
            override fun arity(): Int {
                return 2
            }

            override fun call(
                interpreter: Interpreter,
                arguments: List<Any?>
            ): Any? {
                return arguments[0] as Double +
                        arguments[1] as Double
            }
        }

        assertEquals(2, callable.arity())

        assertEquals(
            5.0,
            callable.call(
                Interpreter(),
                listOf(2.0, 3.0)
            )
        )
    }

    @Test
    fun storesReturnValue() {
        val returnValue = Return(123.0)

        assertEquals(123.0, returnValue.value)
    }

    @Test
    fun storesNilReturnValue() {
        val returnValue = Return(null)

        assertEquals(null, returnValue.value)
    }

    @Test
    fun canBeThrownAndCaught() {
        val returnValue = assertFailsWith<Return> {
            throw Return("hello")
        }

        assertEquals("hello", returnValue.value)
    }
}