# Lox Interpreter 
A Lox interpreter implemented in Kotlin based on the interpreter developed in *Crafting Interpreters*.

**Name:** Makenzie Hale  
**Programming Language:** Kotlin  

## Current Implementation

The interpreter currently includes the functionality implemented through Chapter 7.

At this stage, the interpreter can:

- Read Lox source code from a file or interactive prompt
- Scan source code into tokens
- Parse tokens into expression syntax trees
- Evaluate Lox expressions
- Perform arithmetic, comparison, equality, and string operations
- Apply Lox truthiness rules
- Report scanning, syntax, and runtime errors

### Limitations
The interpreter can scan, parse, and evaluate expressions, but statements and
other features introduced in later chapters are not implemented yet.

## Chapter 7 - Evaluating Expressions Implementation
Chapter 7 adds `Interpreter.kt` for evaluating expression syntax trees produced by the parser
and `RuntimeError.kt` for handling errors that occur during evaluation.

The interpreter supports:

- Literals - numbers, strings, booleans, and `nil`
- Grouping with parentheses
- Unary operators - `!` and `-`
- Arithmetic operators - `*`, `/`, `+`, and `-`
- Comparison operators - `<`, `<=`, `>`, and `>=`
- Equality operators - `==` and `!=`
- String concatenation using `+`
- Lox truthiness rules
- Runtime type checking
- Runtime error reporting

`RuntimeError.kt` represents errors that occur while evaluating expressions. Runtime
errors are reported with the source line where the error occurred.

Expression results are now printed to the console instead of printing the AST.

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

## Testing
### Automated Testing
To run all automated unit tests:
```bash
./gradlew test
```

To clean the previous build and rerun all tests:
```bash
./gradlew clean test
```

The automated tests are organized into the following files:
- `ScannerTest.kt` - Tests scanning, tokenization, literals, keywords, comments, line tracking, and scanner errors.
- `LoxTest.kt` - Tests parsed expressions and Lox syntax and runtime error reporting.
- `ExprTest.kt` - Tests the generated AST expression classes, including stored values, operators, 
expressions, and Visitor routing.
- `AstPrinterTest.kt` - Tests the parenthesized string representation of AST expressions.
- `GenerateAstTest.kt` - Tests AST generation, including expression classes, fields, Visitor methods, and `accept()` methods.
- `ParserTest.kt` - Tests expression parsing, precedence, associativity, grouping, nesting, and parser errors.
- `InterpreterTest.kt` - Tests expression evaluation, literals, grouping, unary and binary operators, truthiness, equality, nested expressions, and runtime errors.
- `LoxIntegrationTest.kt` - Tests the full Scanner -> Parser -> Interpreter pipeline, including file execution, 
expression evaluation, output formatting, command-line exit codes, syntax errors, runtime errors, and interactive prompt behavior.

### Testing Output
Testing output is included in `src/test/resources/output/`.

- `automated-tests.txt` - Contains the results from the complete automated test suite.
- `expression-output.txt` - Contains the output produced by running `expression.lox` through the interpreter.

## Running the Interpreter
### Run from the Command Line
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

A sample file can be run with:
```bash
./gradlew run --args="src/test/resources/expression.lox"
```

**Note:** At the Chapter 7 stage, a Lox source file should contain only one expression. 
Multiple expressions in the same file are not supported yet.

## Project Structure

```text
lox-interpreter/
├── README.md
└── src/
    ├── main/                               
    │   └── kotlin/
    │       ├── lox/                        # Main Lox interpreter source code
    │       │   ├── Interpreter.kt          # Evaluates expression trees
    │       │   ├── RuntimeError.kt         # Represents runtime evaluation errors
    │       │   ├── AstPrinter.kt           # Prints expression trees
    │       │   ├── Expr.kt                 # AST expression classes
    │       │   ├── Lox.kt                  # Main entry point for the interpreter
    │       │   ├── Parser.kt               # Parses tokens into expression trees
    │       │   ├── Scanner.kt              # Scans Lox source code into tokens
    │       │   ├── Token.kt                # Represents individual tokens
    │       │   └── TokenType.kt            # Defines the different token types
    │       └── tool/
    │           └── GenerateAst.kt          # Generates the AST expression classes
    └── test/
        ├── kotlin/
        │   ├── lox/                        # Tests for the interpreter
        │   │   ├── InterpreterTest.kt      
        │   │   ├── AstPrinterTest.kt
        │   │   ├── ExprTest.kt
        │   │   ├── LoxIntegrationTest.kt
        │   │   ├── LoxTest.kt
        │   │   ├── ParserTest.kt
        │   │   └── ScannerTest.kt
        │   └── tool/
        │       └── GenerateAstTest.kt      # Tests the AST generator
        └── resources/                      # Test resources and output
            ├── expression.lox              # Valid expression used to test file execution
            ├── invalid.lox                 # Invalid syntax used to test syntax error handling
            ├── runtime_error.lox           # Valid syntax that produces a runtime error
            └── output/
                ├── automated-tests.txt     # Automated test suite results
                └── expression-output.txt   # Sample interpreter output
```