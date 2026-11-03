package lox

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertSame

class LoxClassTest {

    @Test
    fun stringRepresentationUsesClassName() {
        val klass = LoxClass(
            "Breakfast",
            null,
            emptyMap()
        )

        assertEquals("Breakfast", klass.toString())
    }

    @Test
    fun classWithoutInitializerHasZeroArity() {
        val klass = LoxClass(
            "Breakfast",
            null,
            emptyMap()
        )

        assertEquals(0, klass.arity())
    }

    @Test
    fun callingClassCreatesInstance() {
        val klass = LoxClass(
            "Breakfast",
            null,
            emptyMap()
        )

        val result = klass.call(
            Interpreter(),
            emptyList()
        )

        assertIs<LoxInstance>(result)
        assertEquals("Breakfast instance", result.toString())
    }

    @Test
    fun findMethodReturnsMethod() {
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
            null,
            mapOf("serve" to method)
        )

        assertSame(
            method,
            klass.findMethod("serve")
        )
    }

    @Test
    fun findMethodReturnsNullWhenMethodDoesNotExist() {
        val klass = LoxClass(
            "Breakfast",
            null,
            emptyMap()
        )

        assertNull(klass.findMethod("serve"))
    }

    @Test
    fun initializerDeterminesClassArity() {
        val declaration = Stmt.Function(
            Token(TokenType.IDENTIFIER, "init", null, 1),
            listOf(
                Token(TokenType.IDENTIFIER, "meat", null, 1),
                Token(TokenType.IDENTIFIER, "bread", null, 1)
            ),
            emptyList()
        )

        val initializer = LoxFunction(
            declaration,
            Environment(),
            true
        )

        val klass = LoxClass(
            "Breakfast",
            null,
            mapOf("init" to initializer)
        )

        assertEquals(2, klass.arity())
    }

    @Test
    fun callingClassRunsInitializer() {
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

        val interpreter = Interpreter()

        val classStatement = Stmt.Class(
            Token(TokenType.IDENTIFIER, "Breakfast", null, 1),
            null,
            listOf(initializer)
        )

        Resolver(interpreter).resolve(
            listOf(classStatement)
        )

        val function = LoxFunction(
            initializer,
            interpreter.globals,
            true
        )

        val klass = LoxClass(
            "Breakfast",
            null,
            mapOf("init" to function)
        )

        val result = klass.call(
            interpreter,
            listOf("bacon")
        )

        assertIs<LoxInstance>(result)

        assertEquals(
            "bacon",
            result.get(
                Token(TokenType.IDENTIFIER, "meat", null, 1)
            )
        )
    }

    @Test
    fun findMethodReturnsInheritedMethod() {
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

        val superclass = LoxClass(
            "Breakfast",
            null,
            mapOf("serve" to method)
        )

        val subclass = LoxClass(
            "Brunch",
            superclass,
            emptyMap()
        )

        assertSame(
            method,
            subclass.findMethod("serve")
        )
    }

    @Test
    fun subclassMethodOverridesSuperclassMethod() {
        val superclassDeclaration = Stmt.Function(
            Token(TokenType.IDENTIFIER, "serve", null, 1),
            emptyList(),
            emptyList()
        )

        val superclassMethod = LoxFunction(
            superclassDeclaration,
            Environment(),
            false
        )

        val subclassDeclaration = Stmt.Function(
            Token(TokenType.IDENTIFIER, "serve", null, 1),
            emptyList(),
            emptyList()
        )

        val subclassMethod = LoxFunction(
            subclassDeclaration,
            Environment(),
            false
        )

        val superclass = LoxClass(
            "Breakfast",
            null,
            mapOf("serve" to superclassMethod)
        )

        val subclass = LoxClass(
            "Brunch",
            superclass,
            mapOf("serve" to subclassMethod)
        )

        assertSame(
            subclassMethod,
            subclass.findMethod("serve")
        )
    }

    @Test
    fun findMethodReturnsNullWhenMethodDoesNotExistInClassHierarchy() {
        val superclass = LoxClass(
            "Breakfast",
            null,
            emptyMap()
        )

        val subclass = LoxClass(
            "Brunch",
            superclass,
            emptyMap()
        )

        assertNull(
            subclass.findMethod("serve")
        )
    }

    @Test
    fun inheritedInitializerDeterminesSubclassArity() {
        val declaration = Stmt.Function(
            Token(TokenType.IDENTIFIER, "init", null, 1),
            listOf(
                Token(TokenType.IDENTIFIER, "value", null, 1)
            ),
            emptyList()
        )

        val initializer = LoxFunction(
            declaration,
            Environment(),
            true
        )

        val superclass = LoxClass(
            "Parent",
            null,
            mapOf("init" to initializer)
        )

        val subclass = LoxClass(
            "Child",
            superclass,
            emptyMap()
        )

        assertEquals(1, subclass.arity())
    }
}