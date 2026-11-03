package tool

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertTrue

class GenerateAstTest {

    private fun generateExpr(): String {
        val tempDir = Files.createTempDirectory("ast-test")

        main(arrayOf(tempDir.toString()))

        val exprFile = tempDir.resolve("Expr.kt")
        return Files.readString(exprFile)
    }

    private fun generateStmt(): String {
        val tempDir = Files.createTempDirectory("ast-test")

        main(arrayOf(tempDir.toString()))

        val stmtFile = tempDir.resolve("Stmt.kt")
        return Files.readString(stmtFile)
    }

    @Test
    fun generatesExprFile() {
        val tempDir = Files.createTempDirectory("ast-test")

        main(arrayOf(tempDir.toString()))

        val exprFile = tempDir.resolve("Expr.kt")

        assertTrue(Files.exists(exprFile))
    }

    @Test
    fun generatesExpressionClasses() {
        val content = generateExpr()

        assertTrue(content.contains("class Binary("))
        assertTrue(content.contains("class Grouping("))
        assertTrue(content.contains("class Literal("))
        assertTrue(content.contains("class Unary("))
        assertTrue(content.contains("class Call("))
        assertTrue(content.contains("class Assign("))
        assertTrue(content.contains("class Logical("))
        assertTrue(content.contains("class Variable("))
        assertTrue(content.contains("class Get("))
        assertTrue(content.contains("class Set("))
        assertTrue(content.contains("class This("))
    }

    @Test
    fun generatesExpressionFields() {
        val content = generateExpr()

        assertTrue(content.contains("val left: Expr"))
        assertTrue(content.contains("val operator: Token"))
        assertTrue(content.contains("val right: Expr"))
        assertTrue(content.contains("val expression: Expr"))
        assertTrue(content.contains("val value: Any?"))
        assertTrue(content.contains("val callee: Expr"))
        assertTrue(content.contains("val paren: Token"))
        assertTrue(content.contains("val arguments: List<Expr>"))
        assertTrue(content.contains("val name: Token"))
        assertTrue(content.contains("val obj: Expr"))
        assertTrue(content.contains("val keyword: Token"))
    }

    @Test
    fun generatesVisitorMethods() {
        val content = generateExpr()

        assertTrue(content.contains("interface Visitor<R>"))
        assertTrue(content.contains("fun visitBinaryExpr(expr: Binary): R"))
        assertTrue(content.contains("fun visitGroupingExpr(expr: Grouping): R"))
        assertTrue(content.contains("fun visitLiteralExpr(expr: Literal): R"))
        assertTrue(content.contains("fun visitUnaryExpr(expr: Unary): R"))
        assertTrue(content.contains("fun visitCallExpr(expr: Call): R"))
        assertTrue(content.contains("fun visitAssignExpr(expr: Assign): R"))
        assertTrue(content.contains("fun visitLogicalExpr(expr: Logical): R"))
        assertTrue(content.contains("fun visitVariableExpr(expr: Variable): R"))
        assertTrue(content.contains("fun visitGetExpr(expr: Get): R"))
        assertTrue(content.contains("fun visitSetExpr(expr: Set): R"))
        assertTrue(content.contains("fun visitThisExpr(expr: This): R"))
    }

    @Test
    fun generatesAcceptMethods() {
        val content = generateExpr()

        assertTrue(content.contains("return visitor.visitBinaryExpr(this)"))
        assertTrue(content.contains("return visitor.visitGroupingExpr(this)"))
        assertTrue(content.contains("return visitor.visitLiteralExpr(this)"))
        assertTrue(content.contains("return visitor.visitUnaryExpr(this)"))
        assertTrue(content.contains("abstract fun <R> accept(visitor: Visitor<R>): R"))
        assertTrue(content.contains("return visitor.visitCallExpr(this)"))
        assertTrue(content.contains("return visitor.visitAssignExpr(this)"))
        assertTrue(content.contains("return visitor.visitLogicalExpr(this)"))
        assertTrue(content.contains("return visitor.visitVariableExpr(this)"))
        assertTrue(content.contains("return visitor.visitGetExpr(this)"))
        assertTrue(content.contains("return visitor.visitSetExpr(this)"))
        assertTrue(content.contains("return visitor.visitThisExpr(this)"))
    }

    @Test
    fun generatesStatementClasses() {
        val content = generateStmt()

        assertTrue(content.contains("class Block("))
        assertTrue(content.contains("class Expression("))
        assertTrue(content.contains("class Function("))
        assertTrue(content.contains("class If("))
        assertTrue(content.contains("class Print("))
        assertTrue(content.contains("class Return("))
        assertTrue(content.contains("class Var("))
        assertTrue(content.contains("class While("))
        assertTrue(content.contains("class Class("))
    }

    @Test
    fun generatesStatementVisitorMethods() {
        val content = generateStmt()

        assertTrue(content.contains("fun visitBlockStmt(stmt: Block): R"))
        assertTrue(content.contains("fun visitExpressionStmt(stmt: Expression): R"))
        assertTrue(content.contains("fun visitFunctionStmt(stmt: Function): R"))
        assertTrue(content.contains("fun visitIfStmt(stmt: If): R"))
        assertTrue(content.contains("fun visitPrintStmt(stmt: Print): R"))
        assertTrue(content.contains("fun visitReturnStmt(stmt: Return): R"))
        assertTrue(content.contains("fun visitVarStmt(stmt: Var): R"))
        assertTrue(content.contains("fun visitWhileStmt(stmt: While): R"))
        assertTrue(content.contains("fun visitClassStmt(stmt: Class): R"))
    }

    @Test
    fun generatesStatementAcceptMethods() {
        val content = generateStmt()

        assertTrue(content.contains("return visitor.visitBlockStmt(this)"))
        assertTrue(content.contains("return visitor.visitExpressionStmt(this)"))
        assertTrue(content.contains("return visitor.visitFunctionStmt(this)"))
        assertTrue(content.contains("return visitor.visitIfStmt(this)"))
        assertTrue(content.contains("return visitor.visitPrintStmt(this)"))
        assertTrue(content.contains("return visitor.visitReturnStmt(this)"))
        assertTrue(content.contains("return visitor.visitVarStmt(this)"))
        assertTrue(content.contains("return visitor.visitWhileStmt(this)"))
        assertTrue(content.contains("return visitor.visitClassStmt(this)"))
    }

    @Test
    fun generatesStatementFields() {
        val content = generateStmt()

        assertTrue(content.contains("val statements: List<Stmt>"))
        assertTrue(content.contains("val expression: Expr"))
        assertTrue(content.contains("val name: Token"))
        assertTrue(content.contains("val params: List<Token>"))
        assertTrue(content.contains("val body: List<Stmt>"))
        assertTrue(content.contains("val condition: Expr"))
        assertTrue(content.contains("val thenBranch: Stmt"))
        assertTrue(content.contains("val elseBranch: Stmt?"))
        assertTrue(content.contains("val keyword: Token"))
        assertTrue(content.contains("val value: Expr?"))
        assertTrue(content.contains("val initializer: Expr?"))
        assertTrue(content.contains("val body: Stmt"))
        assertTrue(content.contains("val methods: List<Stmt.Function>"))
    }
}