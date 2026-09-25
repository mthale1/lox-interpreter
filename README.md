# Lox Interpreter 
A Lox interpreter implemented in Kotlin based on the interpreter developed in *Crafting Interpreters*.

**Name:** Makenzie Hale  
**Programming Language:** Kotlin  

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

### Limitations

This implementation currently includes functionality through Chapter 6 of *Crafting Interpreters*.   
Expressions can be scanned and parsed into an AST, but expression evaluation and execution are not implemented yet.

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
- `LoxTest.kt` - Tests expressions through the Scanner -> Parser -> AstPrinter pipeline and tests Lox error reporting.
- `ExprTest.kt` - Tests the generated AST expression classes, including stored values, operators, 
expressions, and Visitor routing.
- `AstPrinterTest.kt` - Tests the parenthesized string representation of AST expressions.
- `GenerateAstTest.kt` - Tests AST generation, including expression classes, fields, Visitor methods, and `accept()` methods.
- `ParserTest.kt` - Tests expression parsing, precedence, associativity, grouping, nesting, and parser errors.
- `LoxIntegrationTest.kt` - Tests file execution, command-line exit codes, usage errors, and interactive prompt behavior.

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

For example, the Chapter 6 sample file can be run with:
```bash
./gradlew run --args="src/test/resources/expression.lox"
```

**At the Chapter 6 stage, the output is the parsed expression tree. Expression evaluation is not implemented yet.**

## Project Structure

```text
lox-interpreter/
├── README.md
└── src/
    ├── main/
    │   └── kotlin/
    │       ├── lox/                        # Main Lox interpreter source code
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
        │   │   ├── AstPrinterTest.kt
        │   │   ├── ExprTest.kt
        │   │   ├── LoxIntegrationTest.kt
        │   │   ├── LoxTest.kt
        │   │   ├── ParserTest.kt
        │   │   └── ScannerTest.kt
        │   └── tool/
        │       └── GenerateAstTest.kt       # Tests the AST generator
        └── resources/                       # Test resources and output
            ├── expression.lox
            ├── invalid.lox
            └── output/
                ├── automated-tests.txt     # Automated test suite results
                └── expression-output.txt   # Sample interpreter output
```