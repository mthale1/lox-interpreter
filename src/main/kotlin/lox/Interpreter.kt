package lox

class Interpreter : Expr.Visitor<Any?>, Stmt.Visitor<Unit> {
    val globals = Environment()
    private var environment = globals
    private val locals = mutableMapOf<Expr, Int>()

    init {
        globals.define("clock", object : LoxCallable {
            override fun arity(): Int {
                return 0
            }

            override fun call(
                interpreter: Interpreter,
                arguments: List<Any?>
            ): Any? {
                return System.currentTimeMillis() / 1000.0
            }

            override fun toString(): String {
                return "<native fn>"
            }
        })
    }

    override fun visitLiteralExpr(expr: Expr.Literal): Any? {
        return expr.value
    }

    override fun visitLogicalExpr(expr: Expr.Logical): Any? {
        val left: Any? = evaluate(expr.left)

        if (expr.operator.type == TokenType.OR) {
            if (isTruthy(left)) return left
        } else {
            if (!isTruthy(left)) return left
        }

        return evaluate(expr.right)
    }

    override fun visitUnaryExpr(expr: Expr.Unary): Any? {
        val right = evaluate(expr.right)

        when (expr.operator.type) {
            TokenType.BANG -> return !isTruthy(right)
            TokenType.MINUS -> {
                checkNumberOperand(expr.operator, right)
                return -(right as Double)
            }
            else -> {}
        }

        // Unreachable.
        return null
    }

    override fun visitVariableExpr(expr: Expr.Variable): Any? {
        return lookUpVariable(expr.name, expr)
    }

    private fun lookUpVariable(name: Token, expr: Expr): Any? {
        val distance = locals[expr]

        return if (distance != null) {
            environment.getAt(distance, name.lexeme)
        } else {
            globals.get(name)
        }
    }

    override fun visitVarStmt(stmt: Stmt.Var) {
        var value: Any? = null

        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer)
        }

        environment.define(stmt.name.lexeme, value)
    }

    override fun visitWhileStmt(stmt: Stmt.While) {
        while (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body)
        }
    }

    override fun visitClassStmt(stmt: Stmt.Class) {
        var superclass: Any? = null

        if (stmt.superclass != null) {
            superclass = evaluate(stmt.superclass)

            if (superclass !is LoxClass) {
                throw RuntimeError(
                    stmt.superclass.name,
                    "Superclass must be a class."
                )
            }
        }

        environment.define(stmt.name.lexeme, null)

        if (stmt.superclass != null) {
            environment = Environment(environment)
            environment.define("super", superclass)
        }

        val methods = mutableMapOf<String, LoxFunction>()

        for (method in stmt.methods) {
            val function = LoxFunction(
                method,
                environment,
                method.name.lexeme == "init"
            )

            methods[method.name.lexeme] = function
        }

        val klass = LoxClass(
            stmt.name.lexeme,
            superclass as LoxClass?,
            methods
        )

        if (superclass != null) {
            environment = environment.enclosing!!
        }

        environment.assign(stmt.name, klass)
    }

    override fun visitGetExpr(expr: Expr.Get): Any? {
        val objectValue = evaluate(expr.obj)

        if (objectValue is LoxInstance) {
            return objectValue.get(expr.name)
        }

        throw RuntimeError(
            expr.name,
            "Only instances have properties."
        )
    }

    override fun visitSetExpr(expr: Expr.Set): Any? {
        val objectValue = evaluate(expr.obj)

        if (objectValue !is LoxInstance) {
            throw RuntimeError(
                expr.name,
                "Only instances have fields."
            )
        }

        val value = evaluate(expr.value)
        objectValue.set(expr.name, value)

        return value
    }

    override fun visitSuperExpr(expr: Expr.Super): Any? {
        val distance = locals[expr]!!

        val superclass = environment.getAt(
            distance,
            "super"
        ) as LoxClass

        val obj = environment.getAt(
            distance - 1,
            "this"
        ) as LoxInstance

        val method = superclass.findMethod(expr.method.lexeme) ?: throw RuntimeError(
            expr.method,
            "Undefined property '${expr.method.lexeme}'."
        )

        return method.bind(obj)
    }

    override fun visitThisExpr(expr: Expr.This): Any? {
        return lookUpVariable(expr.keyword, expr)
    }

    override fun visitAssignExpr(expr: Expr.Assign): Any? {
        val value = evaluate(expr.value)

        val distance = locals[expr]

        if (distance != null) {
            environment.assignAt(distance, expr.name, value)
        } else {
            globals.assign(expr.name, value)
        }

        return value
    }

    private fun checkNumberOperand(operator: Token, operand: Any?) {
        if (operand is Double) return
        throw RuntimeError(operator, "Operand must be a number.")
    }

    private fun checkNumberOperands(operator: Token, left: Any?, right: Any?) {
        if (left is Double && right is Double) return

        throw RuntimeError(operator, "Operands must be numbers.")
    }

    private fun isTruthy(obj: Any?): Boolean {
        if (obj == null) return false
        if (obj is Boolean) return obj
        return true
    }

    private fun isEqual(a: Any?, b: Any?): Boolean {
        if (a == null && b == null) return true
        if (a == null) return false

        return a == b
    }

    private fun stringify(obj: Any?): String {
        if (obj == null) return "nil"

        if (obj is Double) {
            var text = obj.toString()
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length - 2)
            }
            return text
        }

        return obj.toString()
    }

    override fun visitGroupingExpr(expr: Expr.Grouping): Any? {
        return evaluate(expr.expression)
    }

    private fun evaluate(expr: Expr): Any? {
        return expr.accept(this)
    }

    private fun execute(stmt: Stmt) {
        stmt.accept(this)
    }

    fun executeBlock(statements: List<Stmt>, environment: Environment) {
        val previous: Environment = this.environment

        try {
            this.environment = environment

            for (statement in statements) {
                execute(statement)
            }
        } finally {
            this.environment = previous
        }
    }

    override fun visitBlockStmt(stmt: Stmt.Block) {
        executeBlock(stmt.statements, Environment(environment))
    }

    override fun visitExpressionStmt(stmt: Stmt.Expression) {
        evaluate(stmt.expression)
    }

    override fun visitFunctionStmt(stmt: Stmt.Function) {
        val function = LoxFunction(
            stmt,
            environment,
            false
        )
        environment.define(stmt.name.lexeme, function)
    }

    override fun visitIfStmt(stmt: Stmt.If) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch)
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch)
        }
    }

    override fun visitPrintStmt(stmt: Stmt.Print) {
        val value: Any? = evaluate(stmt.expression)
        println(stringify(value))
    }

    override fun visitReturnStmt(stmt: Stmt.Return) {
        var value: Any? = null

        if (stmt.value != null) {
            value = evaluate(stmt.value)
        }

        throw Return(value)
    }

    override fun visitBinaryExpr(expr: Expr.Binary): Any? {
        val left = evaluate(expr.left)
        val right = evaluate(expr.right)

        when (expr.operator.type) {

            TokenType.BANG_EQUAL -> return !isEqual(left, right)
            TokenType.EQUAL_EQUAL -> return isEqual(left, right)

            TokenType.GREATER -> {
                checkNumberOperands(expr.operator, left, right)
                return (left as Double) > (right as Double)
            }

            TokenType.GREATER_EQUAL -> {
                checkNumberOperands(expr.operator, left, right)
                return (left as Double) >= (right as Double)
            }

            TokenType.LESS -> {
                checkNumberOperands(expr.operator, left, right)
                return (left as Double) < (right as Double)
            }

            TokenType.LESS_EQUAL -> {
                checkNumberOperands(expr.operator, left, right)
                return (left as Double) <= (right as Double)
            }

            TokenType.MINUS -> {
                checkNumberOperands(expr.operator, left, right)
                return (left as Double) - (right as Double)
            }

            TokenType.PLUS -> {
                if (left is Double && right is Double) {
                    return left + right
                }

                if (left is String && right is String) {
                    return left + right
                }

                throw RuntimeError(
                    expr.operator,
                    "Operands must be two numbers or two strings."
                )
            }

            TokenType.SLASH -> {
                checkNumberOperands(expr.operator, left, right)
                return (left as Double) / (right as Double)
            }

            TokenType.STAR -> {
                checkNumberOperands(expr.operator, left, right)
                return (left as Double) * (right as Double)
            }

            else -> {}
        }

        // Unreachable.
        return null
    }

    override fun visitCallExpr(expr: Expr.Call): Any? {
        val callee: Any? = evaluate(expr.callee)

        val arguments = mutableListOf<Any?>()

        for (argument in expr.arguments) {
            arguments.add(evaluate(argument))
        }

        if (callee !is LoxCallable) {
            throw RuntimeError(
                expr.paren,
                "Can only call functions and classes."
            )
        }

        val function = callee

        if (arguments.size != function.arity()) {
            throw RuntimeError(
                expr.paren,
                "Expected ${function.arity()} arguments but got ${arguments.size}."
            )
        }

        return function.call(this, arguments)
    }

    fun interpret(statements: List<Stmt>) {
        try {
            for (statement in statements) {
                execute(statement)
            }
        } catch (error: RuntimeError) {
            Lox.runtimeError(error)
        }
    }

    fun resolve(expr: Expr, depth: Int) {
        locals[expr] = depth
    }
}