package lox

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull

class LoxInstanceTest {

    @Test
    fun stringRepresentationUsesClassName() {
        val klass = LoxClass(
            "Breakfast",
            emptyMap()
        )

        val instance = LoxInstance(klass)

        assertEquals(
            "Breakfast instance",
            instance.toString()
        )
    }

    @Test
    fun setsAndGetsField() {
        val klass = LoxClass(
            "Breakfast",
            emptyMap()
        )

        val instance = LoxInstance(klass)

        val name = Token(
            TokenType.IDENTIFIER,
            "meat",
            null,
            1
        )

        instance.set(name, "bacon")

        assertEquals(
            "bacon",
            instance.get(name)
        )
    }

    @Test
    fun fieldCanBeUpdated() {
        val klass = LoxClass(
            "Breakfast",
            emptyMap()
        )

        val instance = LoxInstance(klass)

        val name = Token(
            TokenType.IDENTIFIER,
            "meat",
            null,
            1
        )

        instance.set(name, "bacon")
        instance.set(name, "sausage")

        assertEquals(
            "sausage",
            instance.get(name)
        )
    }

    @Test
    fun fieldCanStoreNil() {
        val klass = LoxClass(
            "Breakfast",
            emptyMap()
        )

        val instance = LoxInstance(klass)

        val name = Token(
            TokenType.IDENTIFIER,
            "meat",
            null,
            1
        )

        instance.set(name, null)

        assertNull(instance.get(name))
    }

    @Test
    fun undefinedPropertyThrowsRuntimeError() {
        val klass = LoxClass(
            "Breakfast",
            emptyMap()
        )

        val instance = LoxInstance(klass)

        val name = Token(
            TokenType.IDENTIFIER,
            "meat",
            null,
            4
        )

        val error = assertFailsWith<RuntimeError> {
            instance.get(name)
        }

        assertEquals(
            "Undefined property 'meat'.",
            error.message
        )

        assertEquals(name, error.token)
    }

    @Test
    fun getsMethodFromClass() {
        val declaration = Stmt.Function(
            Token(TokenType.IDENTIFIER, "serve", null, 1),
            emptyList(),
            emptyList()
        )

        val method = LoxFunction(
            declaration,
            Environment(),
            false
        )

        val klass = LoxClass(
            "Breakfast",
            mapOf("serve" to method)
        )

        val instance = LoxInstance(klass)

        val result = instance.get(
            Token(TokenType.IDENTIFIER, "serve", null, 1)
        )

        assertIs<LoxFunction>(result)
    }

    @Test
    fun methodIsBoundToInstance() {
        val thisExpression = Expr.This(
            Token(TokenType.THIS, "this", null, 1)
        )

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

        val method = LoxFunction(
            declaration,
            Environment(),
            false
        )

        val klass = LoxClass(
            "Breakfast",
            mapOf("getThis" to method)
        )

        val instance = LoxInstance(klass)

        val boundMethod = instance.get(
            Token(TokenType.IDENTIFIER, "getThis", null, 1)
        )

        assertIs<LoxFunction>(boundMethod)

        assertEquals(
            instance,
            boundMethod.call(interpreter, emptyList())
        )
    }

    @Test
    fun fieldShadowsMethodWithSameName() {
        val declaration = Stmt.Function(
            Token(TokenType.IDENTIFIER, "serve", null, 1),
            emptyList(),
            emptyList()
        )

        val method = LoxFunction(
            declaration,
            Environment(),
            false
        )

        val klass = LoxClass(
            "Breakfast",
            mapOf("serve" to method)
        )

        val instance = LoxInstance(klass)

        val name = Token(
            TokenType.IDENTIFIER,
            "serve",
            null,
            1
        )

        instance.set(name, "field value")

        assertEquals(
            "field value",
            instance.get(name)
        )
    }
}