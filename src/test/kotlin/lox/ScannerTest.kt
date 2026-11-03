package lox

import kotlin.test.Test
import kotlin.test.assertEquals
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class ScannerTest {

    private fun assertToken(
        token: Token,
        type: TokenType,
        lexeme: String,
        literal: Any?,
    ) {
        assertEquals(type, token.type)
        assertEquals(lexeme, token.lexeme)
        assertEquals(literal, token.literal)
    }

    private fun captureError(action: () -> Unit): String {
        val output = ByteArrayOutputStream()
        val originalError = System.err

        try {
            System.setErr(PrintStream(output))
            action()
        } finally {
            System.setErr(originalError)
        }

        return output.toString().trim()
    }

    @Test
    fun scansSingleCharacterTokens() {
        val scanner = Scanner("(){},.-+;/*")
        val tokens = scanner.scanTokens()

        assertEquals(TokenType.LEFT_PAREN, tokens[0].type)
        assertEquals(TokenType.RIGHT_PAREN, tokens[1].type)
        assertEquals(TokenType.LEFT_BRACE, tokens[2].type)
        assertEquals(TokenType.RIGHT_BRACE, tokens[3].type)
        assertEquals(TokenType.COMMA, tokens[4].type)
        assertEquals(TokenType.DOT, tokens[5].type)
        assertEquals(TokenType.MINUS, tokens[6].type)
        assertEquals(TokenType.PLUS, tokens[7].type)
        assertEquals(TokenType.SEMICOLON, tokens[8].type)
        assertEquals(TokenType.SLASH, tokens[9].type)
        assertEquals(TokenType.STAR, tokens[10].type)
        assertEquals(TokenType.EOF, tokens[11].type)
    }

    @Test
    fun scansOneOrTwoCharacterOperators() {
        val scanner = Scanner("! != = == < <= > >=")
        val tokens = scanner.scanTokens()

        assertEquals(TokenType.BANG, tokens[0].type)
        assertEquals(TokenType.BANG_EQUAL, tokens[1].type)
        assertEquals(TokenType.EQUAL, tokens[2].type)
        assertEquals(TokenType.EQUAL_EQUAL, tokens[3].type)
        assertEquals(TokenType.LESS, tokens[4].type)
        assertEquals(TokenType.LESS_EQUAL, tokens[5].type)
        assertEquals(TokenType.GREATER, tokens[6].type)
        assertEquals(TokenType.GREATER_EQUAL, tokens[7].type)
        assertEquals(TokenType.EOF, tokens[8].type)
    }


    @Test
    fun scansKeywords() {
        val scanner = Scanner("and class else false for fun if nil or print return super this true var while")
        val tokens = scanner.scanTokens()

        assertEquals(TokenType.AND, tokens[0].type)
        assertEquals(TokenType.CLASS, tokens[1].type)
        assertEquals(TokenType.ELSE, tokens[2].type)
        assertEquals(TokenType.FALSE, tokens[3].type)
        assertEquals(TokenType.FOR, tokens[4].type)
        assertEquals(TokenType.FUN, tokens[5].type)
        assertEquals(TokenType.IF, tokens[6].type)
        assertEquals(TokenType.NIL, tokens[7].type)
        assertEquals(TokenType.OR, tokens[8].type)
        assertEquals(TokenType.PRINT, tokens[9].type)
        assertEquals(TokenType.RETURN, tokens[10].type)
        assertEquals(TokenType.SUPER, tokens[11].type)
        assertEquals(TokenType.THIS, tokens[12].type)
        assertEquals(TokenType.TRUE, tokens[13].type)
        assertEquals(TokenType.VAR, tokens[14].type)
        assertEquals(TokenType.WHILE, tokens[15].type)
    }

    @Test
    fun ignoresWhitespace() {
        val scanner = Scanner(" \t\r")
        val tokens = scanner.scanTokens()

        assertEquals(TokenType.EOF, tokens[0].type)
        assertEquals(1, tokens.size)
    }

    @Test
    fun tracksLineNumbers() {
        val scanner = Scanner("\n\n+")
        val tokens = scanner.scanTokens()

        assertEquals(TokenType.PLUS, tokens[0].type)
        assertEquals(3, tokens[0].line)
    }

    @Test
    fun ignoresComments() {
        val scanner = Scanner("// comment\n+ // comment at end")
        val tokens = scanner.scanTokens()

        assertEquals(TokenType.PLUS, tokens[0].type)
        assertEquals(2, tokens[0].line)
        assertEquals(TokenType.EOF, tokens[1].type)
        assertEquals(2, tokens.size)
    }

    @Test
    fun scansStrings() {
        val scanner = Scanner("\"hello world\" \"hello\nworld\" \"\"")
        val tokens = scanner.scanTokens()

        assertToken(tokens[0], TokenType.STRING, "\"hello world\"", "hello world")
        assertToken(tokens[1], TokenType.STRING, "\"hello\nworld\"", "hello\nworld")
        assertToken(tokens[2], TokenType.STRING, "\"\"", "")

        assertEquals(2, tokens[1].line)
        assertEquals(TokenType.EOF, tokens[3].type)
    }

    @Test
    fun scansIntegers() {
        val scanner = Scanner("12345 0 006")
        val tokens = scanner.scanTokens()

        assertToken(tokens[0], TokenType.NUMBER, "12345", 12345.0)
        assertToken(tokens[1], TokenType.NUMBER, "0", 0.0)
        assertToken(tokens[2], TokenType.NUMBER, "006", 6.0)

        assertEquals(TokenType.EOF, tokens[3].type)
    }

    @Test
    fun scansDecimals() {
        val scanner = Scanner("123.45 0.5 6.0 .45 12.34.56 123.")
        val tokens = scanner.scanTokens()

        assertToken(tokens[0], TokenType.NUMBER, "123.45", 123.45)
        assertToken(tokens[1], TokenType.NUMBER, "0.5", 0.5)
        assertToken(tokens[2], TokenType.NUMBER, "6.0", 6.0)

        assertToken(tokens[3], TokenType.DOT, ".", null)
        assertToken(tokens[4], TokenType.NUMBER, "45", 45.0)

        assertToken(tokens[5], TokenType.NUMBER, "12.34", 12.34)
        assertToken(tokens[6], TokenType.DOT, ".", null)
        assertToken(tokens[7], TokenType.NUMBER, "56", 56.0)

        assertToken(tokens[8], TokenType.NUMBER, "123", 123.0)
        assertToken(tokens[9], TokenType.DOT, ".", null)

        assertEquals(TokenType.EOF, tokens[10].type)
    }

    @Test
    fun scansIdentifiers() {
        val scanner = Scanner("hello hello123 hello_world _hello HELLO orchid whileLoop various")
        val tokens = scanner.scanTokens()

        assertToken(tokens[0], TokenType.IDENTIFIER, "hello", null)
        assertToken(tokens[1], TokenType.IDENTIFIER, "hello123", null)
        assertToken(tokens[2], TokenType.IDENTIFIER, "hello_world", null)
        assertToken(tokens[3], TokenType.IDENTIFIER, "_hello", null)
        assertToken(tokens[4], TokenType.IDENTIFIER, "HELLO", null)
        assertToken(tokens[5], TokenType.IDENTIFIER, "orchid", null)
        assertToken(tokens[6], TokenType.IDENTIFIER, "whileLoop", null)
        assertToken(tokens[7], TokenType.IDENTIFIER, "various", null)

        assertEquals(TokenType.EOF, tokens[8].type)
    }

    @Test
    fun identifierCannotStartWithNumber() {
        val scanner = Scanner("123hello")
        val tokens = scanner.scanTokens()

        assertToken(tokens[0], TokenType.NUMBER, "123", 123.0)
        assertToken(tokens[1], TokenType.IDENTIFIER, "hello", null)

        assertEquals(TokenType.EOF, tokens[2].type)
    }

    @Test
    fun scansEmptySource() {
        val scanner = Scanner("")
        val tokens = scanner.scanTokens()

        assertEquals(TokenType.EOF, tokens[0].type)
        assertEquals(1, tokens.size)
    }

    @Test
    fun rejectsUnterminatedString() {
        lateinit var tokens: List<Token>

        val error = captureError {
            tokens = Scanner("\"hello world").scanTokens()
        }

        assertEquals("[line 1] Error: Unterminated string.", error)
        assertEquals(TokenType.EOF, tokens[0].type)
        assertEquals(1, tokens.size)
    }

    @Test
    fun rejectsMultilineUnterminatedString() {
        lateinit var tokens: List<Token>

        val error = captureError {
            tokens = Scanner("\"hello\nworld").scanTokens()
        }

        assertEquals("[line 2] Error: Unterminated string.", error)
        assertEquals(TokenType.EOF, tokens[0].type)
        assertEquals(1, tokens.size)
    }

    @Test
    fun rejectsUnexpectedCharacter() {
        lateinit var tokens: List<Token>

        val error = captureError {
            tokens = Scanner("@+").scanTokens()
        }

        assertEquals("[line 1] Error: Unexpected character.", error)
        assertEquals(TokenType.PLUS, tokens[0].type)
        assertEquals(TokenType.EOF, tokens[1].type)
        assertEquals(2, tokens.size)
    }

    @Test
    fun scansLoxSource() {
        val scanner = Scanner(
            """
                var language = "lox";
                print language;
            """.trimIndent()
        )
        val tokens = scanner.scanTokens()

        assertToken(tokens[0], TokenType.VAR, "var", null)
        assertToken(tokens[1], TokenType.IDENTIFIER, "language", null)
        assertToken(tokens[2], TokenType.EQUAL, "=", null)
        assertToken(tokens[3], TokenType.STRING, "\"lox\"", "lox")
        assertToken(tokens[4], TokenType.SEMICOLON, ";", null)

        assertEquals(1, tokens[0].line)
        assertEquals(2, tokens[5].line)

        assertToken(tokens[5], TokenType.PRINT, "print", null)
        assertToken(tokens[6], TokenType.IDENTIFIER, "language", null)
        assertToken(tokens[7], TokenType.SEMICOLON, ";", null)

        assertEquals(TokenType.EOF, tokens[8].type)
    }

}