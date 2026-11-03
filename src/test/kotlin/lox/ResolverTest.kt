package lox

import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.Test
import kotlin.test.assertTrue

class ResolverTest {

    private fun resolve(source: String): String {
        val output = ByteArrayOutputStream()
        val originalError = System.err

        try {
            System.setErr(PrintStream(output))

            val tokens = Scanner(source).scanTokens()
            val statements = Parser(tokens).parse()

            val interpreter = Interpreter()
            val resolver = Resolver(interpreter)
            resolver.resolve(statements)
        } finally {
            System.setErr(originalError)
        }

        return output.toString().trim()
    }

    @Test
    fun rejectsVariableInOwnInitializer() {
        val error = resolve(
            """
            {
                var a = a;
            }
            """.trimIndent()
        )

        assertTrue(
            error.contains(
                "Can't read local variable in its own initializer."
            )
        )
    }

    @Test
    fun rejectsDuplicateVariableInSameScope() {
        val error = resolve(
            """
            {
                var a = 1;
                var a = 2;
            }
            """.trimIndent()
        )

        assertTrue(
            error.contains(
                "Already a variable with this name in this scope."
            )
        )
    }

    @Test
    fun rejectsReturnFromTopLevelCode() {
        val error = resolve(
            """
            return 123;
            """.trimIndent()
        )

        assertTrue(
            error.contains(
                "Can't return from top-level code."
            )
        )
    }

    @Test
    fun allowsVariableShadowingInNestedScope() {
        val error = resolve(
            """
        {
            var a = "outer";
            {
                var a = "inner";
                print a;
            }
        }
        """.trimIndent()
        )

        assertTrue(error.isEmpty())
    }

    @Test
    fun resolvesFunctionParameters() {
        val error = resolve(
            """
        fun identity(value) {
            return value;
        }
        """.trimIndent()
        )

        assertTrue(error.isEmpty())
    }

    @Test
    fun allowsReturnInsideFunction() {
        val error = resolve(
            """
        fun test() {
            return 123;
        }
        """.trimIndent()
        )

        assertTrue(error.isEmpty())
    }

    @Test
    fun allowsSameVariableNameInDifferentScopes() {
        val error = resolve(
            """
        {
            var a = 1;
        }

        {
            var a = 2;
        }
        """.trimIndent()
        )

        assertTrue(error.isEmpty())
    }

    @Test
    fun resolvesNestedFunctionScopes() {
        val error = resolve(
            """
        fun outer(a) {
            var b = 2;

            fun inner(c) {
                print a;
                print b;
                print c;
            }

            inner(3);
        }
        """.trimIndent()
        )

        assertTrue(error.isEmpty())
    }

    @Test
    fun resolvesClassDeclaration() {
        val error = resolve(
            """
        class Breakfast {
            cook() {
                print "eggs";
            }
        }
        """.trimIndent()
        )

        assertTrue(error.isEmpty())
    }

    @Test
    fun resolvesThisInsideMethod() {
        val error = resolve(
            """
        class Breakfast {
            serve() {
                print this;
            }
        }
        """.trimIndent()
        )

        assertTrue(error.isEmpty())
    }

    @Test
    fun resolvesThisInsideNestedFunctionInMethod() {
        val error = resolve(
            """
        class Breakfast {
            serve() {
                fun showThis() {
                    print this;
                }

                showThis();
            }
        }
        """.trimIndent()
        )

        assertTrue(error.isEmpty())
    }

    @Test
    fun rejectsThisOutsideClass() {
        val error = resolve(
            """
        print this;
        """.trimIndent()
        )

        assertTrue(
            error.contains(
                "Can't use 'this' outside of a class."
            )
        )
    }

    @Test
    fun allowsInitializerWithoutReturnValue() {
        val error = resolve(
            """
        class Breakfast {
            init(meat) {
                this.meat = meat;
                return;
            }
        }
        """.trimIndent()
        )

        assertTrue(error.isEmpty())
    }

    @Test
    fun rejectsReturnValueFromInitializer() {
        val error = resolve(
            """
        class Breakfast {
            init() {
                return "something else";
            }
        }
        """.trimIndent()
        )

        assertTrue(
            error.contains(
                "Can't return a value from an initializer."
            )
        )
    }

    @Test
    fun allowsReturnValueFromNormalMethod() {
        val error = resolve(
            """
        class Breakfast {
            serve() {
                return "breakfast";
            }
        }
        """.trimIndent()
        )

        assertTrue(error.isEmpty())
    }

    @Test
    fun resolvesSubclass() {
        val error = resolve(
            """
        class Breakfast {}

        class Brunch < Breakfast {
            serve() {
                print this;
            }
        }
        """.trimIndent()
        )

        assertTrue(error.isEmpty())
    }

    @Test
    fun rejectsClassInheritingFromItself() {
        val error = resolve(
            """
        class Breakfast < Breakfast {}
        """.trimIndent()
        )

        assertTrue(
            error.contains(
                "A class can't inherit from itself."
            )
        )
    }

    @Test
    fun resolvesSuperInsideSubclass() {
        val error = resolve(
            """
        class Breakfast {
            serve() {
                print "breakfast";
            }
        }

        class Brunch < Breakfast {
            serve() {
                super.serve();
            }
        }
        """.trimIndent()
        )

        assertTrue(error.isEmpty())
    }

    @Test
    fun rejectsSuperOutsideClass() {
        val error = resolve(
            """
        super.serve();
        """.trimIndent()
        )

        assertTrue(
            error.contains(
                "Can't use 'super' outside of a class."
            )
        )
    }

    @Test
    fun rejectsSuperInClassWithoutSuperclass() {
        val error = resolve(
            """
        class Breakfast {
            serve() {
                super.serve();
            }
        }
        """.trimIndent()
        )

        assertTrue(
            error.contains(
                "Can't use 'super' in a class with no superclass."
            )
        )
    }

    @Test
    fun resolvesSuperInsideNestedFunctionInSubclass() {
        val error = resolve(
            """
        class Parent {
            method() {}
        }

        class Child < Parent {
            method() {
                fun callSuper() {
                    super.method();
                }

                callSuper();
            }
        }
        """.trimIndent()
        )

        assertTrue(error.isEmpty())
    }
}