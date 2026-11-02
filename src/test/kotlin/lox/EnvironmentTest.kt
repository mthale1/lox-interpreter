package lox

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class EnvironmentTest {

    @Test
    fun definesAndGetsVariable() {
        val environment = Environment()
        val name = Token(TokenType.IDENTIFIER, "a", null, 1)

        environment.define("a", 123.0)

        assertEquals(123.0, environment.get(name))
    }

    @Test
    fun definesAndGetsNilVariable() {
        val environment = Environment()
        val name = Token(TokenType.IDENTIFIER, "a", null, 1)

        environment.define("a", null)

        assertEquals(null, environment.get(name))
    }

    @Test
    fun assignsVariable() {
        val environment = Environment()
        val name = Token(TokenType.IDENTIFIER, "a", null, 1)

        environment.define("a", 123.0)
        environment.assign(name, 456.0)

        assertEquals(456.0, environment.get(name))
    }

    @Test
    fun getUndefinedVariableThrowsError() {
        val environment = Environment()
        val name = Token(TokenType.IDENTIFIER, "a", null, 1)

        assertFailsWith<RuntimeError> {
            environment.get(name)
        }
    }

    @Test
    fun assignUndefinedVariableThrowsError() {
        val environment = Environment()
        val name = Token(TokenType.IDENTIFIER, "a", null, 1)

        assertFailsWith<RuntimeError> {
            environment.assign(name, 123.0)
        }
    }

    @Test
    fun getsVariableFromOuterEnvironment() {
        val outer = Environment()
        val inner = Environment(outer)
        val name = Token(TokenType.IDENTIFIER, "a", null, 1)

        outer.define("a", 123.0)

        assertEquals(123.0, inner.get(name))
    }

    @Test
    fun getsVariableThroughMultipleOuterEnvironments() {
        val outer = Environment()
        val middle = Environment(outer)
        val inner = Environment(middle)
        val name = Token(TokenType.IDENTIFIER, "a", null, 1)

        outer.define("a", 123.0)

        assertEquals(123.0, inner.get(name))
    }

    @Test
    fun assignsVariableInOuterEnvironment() {
        val outer = Environment()
        val inner = Environment(outer)
        val name = Token(TokenType.IDENTIFIER, "a", null, 1)

        outer.define("a", 123.0)
        inner.assign(name, 456.0)

        assertEquals(456.0, outer.get(name))
    }

    @Test
    fun innerVariableOverridesOuterVariable() {
        val outer = Environment()
        val inner = Environment(outer)
        val name = Token(TokenType.IDENTIFIER, "a", null, 1)

        outer.define("a", 123.0)
        inner.define("a", 456.0)

        assertEquals(456.0, inner.get(name))
        assertEquals(123.0, outer.get(name))
    }

    @Test
    fun assignsInnerVariableInsteadOfOuterVariable() {
        val outer = Environment()
        val inner = Environment(outer)
        val name = Token(TokenType.IDENTIFIER, "a", null, 1)

        outer.define("a", 123.0)
        inner.define("a", 456.0)

        inner.assign(name, 789.0)

        assertEquals(789.0, inner.get(name))
        assertEquals(123.0, outer.get(name))
    }

}