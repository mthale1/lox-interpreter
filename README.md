# Lox Interpreter 
A Lox interpreter implemented in Kotlin based on the interpreter developed in *Crafting Interpreters*.

**Name:** Makenzie Hale  
**Programming Language:** Kotlin  

## Current Implementation

The interpreter currently includes the functionality implemented through Chapter 9.

At this stage, the interpreter can:
- Read Lox source code from a file or interactive prompt
- Scan source code into tokens and parse it into syntax trees
- Evaluate expressions and execute statements
- Support variables, assignment, and block scope
- Support arithmetic, comparison, equality, and logical operators
- Execute conditional statements and loops
- Report scanning, syntax, and runtime errors

### Limitations
The interpreter currently implements functionality through Chapter 9 of *Crafting Interpreters*. 
Features introduced in later chapters have not yet been implemented.

## Chapter 8 & 9 - Statements, State, and Control Flow  
Chapter 8 and 9 introduce statements, variable environments, and control flow, allowing Lox programs to store
values, manage scope, make decisions, and repeat instructions.

The interpreter supports:
- Expression and print statements
- Variable declarations, access, and assignments
- Global and local environments with nested block scope
- `if` and `else` statements
- `while` and `for` loops
- Logical operators using `and` and `or`
- Runtime error handling for undefined variables

`Environment.kt` manages variable storage, lookup, assignment, and nested environments.  
`Stmt.kt` defines the different statement types used by the parser and interpreter.
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
- `StmtTest.kt` - Tests AST statement classes, stored values, and Visitor routing.
- `AstPrinterTest.kt` - Tests the parenthesized string representation of AST expressions.
- `EnvironmentTest.kt` - Tests variable storage, lookup, assignment, and nested environments.
- `GenerateAstTest.kt` - Tests AST generation, including expression classes, fields, Visitor methods, and `accept()` methods.
- `ParserTest.kt` - Tests expression parsing, precedence, associativity, grouping, nesting, and parser errors.
- `InterpreterTest.kt` - Tests expression evaluation, literals, grouping, unary and binary operators, truthiness, equality, nested expressions, and runtime errors.
- `LoxIntegrationTest.kt` - Tests the full Scanner -> Parser -> Interpreter pipeline, including file execution, 
expression evaluation, output formatting, command-line exit codes, syntax errors, runtime errors, and interactive prompt behavior.

### Testing Output
Testing output is included in `src/test/resources/output/`.

- `automated-tests.txt` - Contains the results from the complete automated test suite.
- `test_output.txt` - Contains the output produced by running `test_input.lox`.
- `expected_test_output.txt` - Contains the expected output for comparison with the actual output.

The files can be compared using:
```bash
diff src/test/resources/output/expected_test_output.txt src/test/resources/output/test_output.txt
```

## Running the Interpreter
### Run from the Command Line
Start the Lox interpreter with:
```bash
./gradlew run --console=plain
```
Enter Lox statements into the interactive prompt. Press `Ctrl+D` to exit.

### Run a Lox Source File
To run a Lox source file:
```bash
./gradlew run --args="path/to/file.lox"
```

A sample file can be run with:
```bash
./gradlew run --args="src/test/resources/input/expression.lox"
```

## Project Structure

```text
lox-interpreter/
├── README.md
└── src/
    ├── main/
    │   └── kotlin/
    │       ├── lox/                        # Main Lox interpreter source code
    │       │   ├── AstPrinter.kt           # Prints expression trees
    │       │   ├── Environment.kt          # Manages variable storage and scope
    │       │   ├── Expr.kt                 # AST expression classes
    │       │   ├── Interpreter.kt          # Evaluates expressions and executes statements
    │       │   ├── Lox.kt                  # Main entry point for the interpreter
    │       │   ├── Parser.kt               # Parses tokens into expression and statement trees
    │       │   ├── RuntimeError.kt         # Represents runtime evaluation errors
    │       │   ├── Scanner.kt              # Scans Lox source code into tokens
    │       │   ├── Stmt.kt                 # AST statement classes
    │       │   ├── Token.kt                # Represents individual tokens
    │       │   └── TokenType.kt            # Defines the different token types
    │       └── tool/
    │           └── GenerateAst.kt          # Generates the AST expression classes
    └── test/
        ├── kotlin/
        │   ├── lox/                        # Tests for the interpreter
        │   │   ├── AstPrinterTest.kt
        │   │   ├── EnvironmentTest.kt
        │   │   ├── ExprTest.kt
        │   │   ├── InterpreterTest.kt
        │   │   ├── LoxIntegrationTest.kt
        │   │   ├── LoxTest.kt
        │   │   ├── ParserTest.kt
        │   │   ├── ScannerTest.kt
        │   │   └── StmtTest.kt
        │   └── tool/
        │       └── GenerateAstTest.kt
        └── resources/                      # Test resources and output
            ├── input/                      # Lox source files used for testing
            │   ├── block_scope.lox
            │   ├── for.lox
            │   ├── if_else.lox
            │   ├── invalid.lox
            │   ├── logical.lox
            │   ├── runtime_error.lox
            │   ├── test_input.lox
            │   ├── undefined_variable.lox
            │   ├── variables.lox
            │   └── while.lox
            └── output/                     # Saved test results and expected output
                ├── automated-tests.txt
                ├── expected_test_output.txt
                └── test_output.txt
```