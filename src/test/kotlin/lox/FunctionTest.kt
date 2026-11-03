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
            Environment(),
            false
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

        val interpreter = Interpreter()
        val resolver = Resolver(interpreter)

        resolver.resolve(listOf(declaration))

        val function = LoxFunction(
            declaration,
            Environment(),
            false
        )

        assertEquals(
            123.0,
            function.call(
                interpreter,
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
            Environment(),
            false
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

        val variable = Expr.Variable(name)

        val declaration = Stmt.Function(
            Token(TokenType.IDENTIFIER, "getA", null, 1),
            emptyList(),
            listOf(
                Stmt.Return(
                    Token(TokenType.RETURN, "return", null, 1),
                    variable
                )
            )
        )

        val interpreter = Interpreter()

        interpreter.resolve(variable, 1)

        val function = LoxFunction(
            declaration,
            closure,
            false
        )

        assertEquals(
            123.0,
            function.call(
                interpreter,
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
            Environment(),
            false
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

    @Test
    fun bindMakesThisAvailableToMethod() {
        val thisToken = Token(TokenType.THIS, "this", null, 1)
        val thisExpression = Expr.This(thisToken)

        val declaration = Stmt.Function(
            Token(TokenType.IDENTIFIER, "getThis", null, 1),
            emptyList(),
            listOf(
                Stmt.Return(
                    Token(TokenType.RETURN, "return", null, 1),
                    thisExpression
                )
            )
        )

        val interpreter = Interpreter()
        interpreter.resolve(thisExpression, 1)

        val function = LoxFunction(
            declaration,
            Environment(),
            false
        )

        val klass = LoxClass("Test", emptyMap())
        val instance = LoxInstance(klass)

        val bound = function.bind(instance)

        assertEquals(
            instance,
            bound.call(interpreter, emptyList())
        )
    }

    @Test
    fun initializerReturnsInstanceWithoutExplicitReturn() {
        val declaration = Stmt.Function(
            Token(TokenType.IDENTIFIER, "init", null, 1),
            emptyList(),
            emptyList()
        )

        val function = LoxFunction(
            declaration,
            Environment(),
            true
        )

        val klass = LoxClass("Test", emptyMap())
        val instance = LoxInstance(klass)

        val bound = function.bind(instance)

        assertEquals(
            instance,
            bound.call(Interpreter(), emptyList())
        )
    }

    @Test
    fun initializerReturnsInstanceAfterEmptyReturn() {
        val declaration = Stmt.Function(
            Token(TokenType.IDENTIFIER, "init", null, 1),
            emptyList(),
            listOf(
                Stmt.Return(
                    Token(TokenType.RETURN, "return", null, 1),
                    null
                )
            )
        )

        val function = LoxFunction(
            declaration,
            Environment(),
            true
        )

        val klass = LoxClass("Test", emptyMap())
        val instance = LoxInstance(klass)

        val bound = function.bind(instance)

        assertEquals(
            instance,
            bound.call(Interpreter(), emptyList())
        )
    }
}