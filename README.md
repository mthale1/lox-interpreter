# Lox Interpreter

A Lox interpreter implemented in Kotlin based on the interpreter developed in *Crafting Interpreters*.

## Current Implementation

The interpreter currently includes the functionality implemented through Chapters 4–6.

At this stage, the interpreter can:

- Read Lox source code from a file or through the interactive prompt
- Scan source code into tokens
- Recognize literals, keywords, identifiers, operators, and punctuation
- Ignore whitespace and comments
- Report scanning errors
- Represent expressions using an Abstract Syntax Tree (AST)
- Parse expressions with the correct precedence and associativity
- Report syntax errors during parsing
- Print parsed expression trees using `AstPrinter`

**The interpreter does not evaluate expressions yet. Valid Lox expressions are currently 
scanned, parsed into an AST, and printed.**

## Chapter 6 - Parsing Expressions Implementation
Chapter 6 adds `Parser.kt`, which uses recursive descent parsing to convert tokens 
from the scanner into expression syntax trees.

The parser supports:

- Literals - numbers, strings, booleans, and `nil`
- Grouping with parentheses
- Unary operators - `!` and `-`
- Arithmetic operators - `*`, `/`, `+`, and `-`
- Comparison operators - `<`, `<=`, `>`, and `>=`
- Equality operators - `==` and `!=`
- Operator precedence and associativity
- Syntax error reporting

`AstPrinter.kt` prints the parsed expression tree in a parenthesized format.

---

## Requirements 
- Java 21 or later

This project includes the Gradle Wrapper, so Gradle does not need to be installed separately.

## Build
Make sure you are in the `lox-interpreter` directory before running the build commands. 
If needed, navigate to the project directory:
```bash
cd lox-interpreter
```

Build the project using the Gradle Wrapper:
```bash
./gradlew build
```
This compiles the project and runs the automated test suite.

## Automated Testing
Run all automated unit tests:
```bash
./gradlew test
```

To clean the previous build and rerun all tests:
```bash
./gradlew clean test
```

The automated tests are organized into the following files:
- `ScannerTest.kt` - Tests scanning, tokenization, literals, keywords, comments, line tracking, and scanner errors.
- `LoxTest.kt` - Tests expressions through the Scanner -> Parser -> AstPrinter pipeline and tests Lox error reporting.
- `ExprTest.kt` - Tests the generated AST expression classes, including stored values, operators, 
expressions, and Visitor routing.
- `AstPrinterTest.kt` - Tests the parenthesized string representation of AST expressions.
- `GenerateAstTest.kt` - Tests AST generation, including expression classes, fields, Visitor methods, and `accept()` methods.
- `ParserTest.kt` - Tests expression parsing, precedence, associativity, grouping, nesting, and parser errors.
- `LoxIntegrationTest.kt` - Tests file execution, command-line exit codes, usage errors, and interactive prompt behavior.

## Running the Interpreter

Start the Lox interpreter with:
```bash
./gradlew run --console=plain
```

Enter one Lox expression at a time into the prompt. Press `Ctrl+D` to exit.

### Run a Lox Source File
To run a Lox source file:
```bash
./gradlew run --args="path/to/file.lox"
```

For example, the Chapter 6 sample file can be run with:
```bash
./gradlew run --args="src/test/resources/expression.lox"
```

The sample file contains:
```lox
!(1 + 2 > 3)
```

Expected output:
```text
(! (group (> (+ 1.0 2.0) 3.0)))
```

**At the Chapter 6 stage, the output is the parsed expression tree. Expression evaluation is not implemented yet.**

## Project Structure

```text
src/
├── main/
│   └── kotlin/
│       ├── lox/                       # Main Lox interpreter source code
│       │   ├── AstPrinter.kt          # Prints expression trees
│       │   ├── Expr.kt                # AST expression classes
│       │   ├── Lox.kt                 # Main entry point for the interpreter
│       │   ├── Parser.kt              # Parses tokens into expression trees
│       │   ├── Scanner.kt             # Scans Lox source code into tokens
│       │   ├── Token.kt               # Represents individual tokens
│       │   └── TokenType.kt           # Defines the different token types
│       └── tool/
│           └── GenerateAst.kt          # Generates the AST expression classes
│   
│
└── test/
    ├── kotlin/
    │   ├── lox/                        # Tests for the interpreter
    │   │   ├── AstPrinterTest.kt
    │   │   ├── ExprTest.kt
    │   │   ├── LoxIntegrationTest.kt
    │   │   ├── LoxTest.kt
    │   │   ├── ParserTest.kt
    │   │   └── ScannerTest.kt
    │   └── tool/
    │       └── GenerateAstTest.kt       # Tests the AST generator
    └── resources/                       # Lox files used for testing
        ├── expression.lox
        └── invalid.lox
```