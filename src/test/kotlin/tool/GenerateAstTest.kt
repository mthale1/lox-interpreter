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
    }

    @Test
    fun generatesExpressionFields() {
        val content = generateExpr()

        assertTrue(content.contains("val left: Expr"))
        assertTrue(content.contains("val operator: Token"))
        assertTrue(content.contains("val right: Expr"))
        assertTrue(content.contains("val expression: Expr"))
        assertTrue(content.contains("val value: Any?"))
    }

    @Test
    fun generatesVisitorMethods() {
        val content = generateExpr()

        assertTrue(content.contains("interface Visitor<R>"))
        assertTrue(content.contains("fun visitBinaryExpr(expr: Binary): R"))
        assertTrue(content.contains("fun visitGroupingExpr(expr: Grouping): R"))
        assertTrue(content.contains("fun visitLiteralExpr(expr: Literal): R"))
        assertTrue(content.contains("fun visitUnaryExpr(expr: Unary): R"))
    }

    @Test
    fun generatesAcceptMethods() {
        val content = generateExpr()

        assertTrue(content.contains("return visitor.visitBinaryExpr(this)"))
        assertTrue(content.contains("return visitor.visitGroupingExpr(this)"))
        assertTrue(content.contains("return visitor.visitLiteralExpr(this)"))
        assertTrue(content.contains("return visitor.visitUnaryExpr(this)"))
        assertTrue(content.contains("abstract fun <R> accept(visitor: Visitor<R>): R"))
    }
}