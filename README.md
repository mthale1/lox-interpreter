# Lox Interpreter

A Lox interpreter implemented in Kotlin based on *Crafting Interpreters*.  

**This submission contains the implementation through Chapter 5, "Representing Code".**

## Requirements 
- Java 21 or later

This project includes the Gradle Wrapper, so Gradle does not need to be installed separately.

## Build
From the project root, run:
```bash
./gradlew build
```

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
- `ScannerTest.kt` - Tests scanning and tokenization.
- `LoxTest.kt` - Tests the Lox program's error reporting behavior.
- `ExprTest.kt` - Tests the generated AST expression classes, including stored values, operators, 
expressions, and Visitor routing.
- `AstPrinterTest.kt` - Tests the parenthesized string representation of AST expressions.
- `GenerateAstTest.kt` - Tests AST generation, including expression classes, fields, Visitor methods, and `accept()` methods.

## Running the Interpreter

Start the Lox interpreter with:
```bash
./gradlew run
```
You can enter Lox code directly into the prompt.

### Run a Lox Source File
An example Lox source file is provided at:
`src/test/resources/chapter4.lox`

Run the provided file with:
```bash
./gradlew run --args="src/test/resources/chapter4.lox"
```

A different Lox source file can be tested by supplying its path:
```bash
./gradlew run --args="path/to/file.lox"
```

## Chapter 5 Implementation
Chapter 5 adds an Abstract Syntax Tree (AST) representation for Lox expressions.

The AST includes the following expression types:
- `Literal` - stores literal values such as numbers, strings, booleans, and `nil`
- `Grouping` - stores a grouped expression
- `Unary` - stores a unary operator and its right-hand expression
- `Binary` - stores a left expression, operator, and right expression

`GenerateAst.kt` is used to generate the expression classes in `Expr.kt`.

The generated expression classes use the Visitor pattern. Each expression implements 
an `accept()` method that routes the expression to the appropriate Visitor method.

`AstPrinter.kt` uses the Visitor pattern to convert an expression tree into a parenthesized string representation.

**Note:** At the Chapter 5 stage, the interpreter can scan Lox source code into tokens.  
Expression syntax trees can be constructed and printed using the AST classes,
but Lox source code is not yet parsed into syntax trees or executed.
