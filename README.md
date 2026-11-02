# Lox Interpreter 
A Lox interpreter implemented in Kotlin based on the interpreter developed in *Crafting Interpreters*.

**Name:** Makenzie Hale  
**Programming Language:** Kotlin  

## Current Implementation

The interpreter currently includes the functionality implemented through Chapter 11.

At this stage, the interpreter can:
- Read Lox source code from a file or interactive prompt
- Scan source code into tokens and parse it into syntax trees
- Evaluate expressions and execute statements
- Support variables, assignment, and block scope
- Support arithmetic, comparison, equality, and logical operators
- Execute conditional statements and loops
- Declare and call functions
- Pass arguments to functions and return values
- Support local function scope and closures
- Support recursive functions
- Provide the native clock() function
- Report scanning, syntax, resolution, and runtime errors
- Resolve local variables using lexical scope
- Support variable shadowing and resolved local assignment
- Detect invalid local variable declarations and top-level return statements

### Limitations
The interpreter currently implements functionality through Chapter 11 of *Crafting Interpreters*. 
Features introduced in later chapters have not yet been implemented.
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
The saved automated test results are located in `src/test/resources/output/automated-tests.txt`.

To run all automated unit tests:
```bash
./gradlew test
```

To clean the previous build and rerun all tests:
```bash
./gradlew clean test
```

To rerun all automated tests and save the results:
```bash
./gradlew test --rerun-tasks > src/test/resources/output/automated-tests.txt 2>&1
```

The automated tests are organized into the following files:
- `ScannerTest.kt` - Tests scanning, tokenization, literals, keywords, comments, line tracking, and scanner errors.
- `LoxTest.kt` - Tests parsed expressions and Lox syntax and runtime error reporting.
- `ExprTest.kt` - Tests the generated AST expression classes, including stored values, operators, 
expressions, and Visitor routing.
- `StmtTest.kt` - Tests AST statement classes, stored values, and Visitor routing.
- `AstPrinterTest.kt` - Tests the parenthesized string representation of AST expressions.
- `EnvironmentTest.kt` - Tests variable storage, lookup, assignment, nested environments, and resolved variable 
access by scope distance.
- `FunctionTest.kt` - Tests callable behavior, user-defined functions, return values, function arity, and closures.
- `GenerateAstTest.kt` - Tests AST generation, including expression classes, fields, Visitor methods, 
and `accept()` methods.
- `ParserTest.kt` - Tests expression parsing, precedence, associativity, grouping, nesting, and parser errors.
- `ResolverTest.kt` - Tests lexical scope resolution, variable declarations, shadowing, function parameters, 
nested function scopes, and resolver errors.
- `InterpreterTest.kt` - Tests expression evaluation, literals, grouping, unary and binary operators, truthiness, 
equality, nested expressions, and runtime errors.
- `LoxIntegrationTest.kt` - Tests the full Scanner -> Parser -> Resolver -> Interpreter pipeline, including file 
execution, expression evaluation, lexical scope resolution, output formatting, command-line exit codes, syntax errors, 
resolver errors, runtime errors, and interactive prompt behavior.

### Sample Input and Output
`src/test/resources/input/test_input.lox` is a sample Lox program included with the project. 
It contains Lox statements that exercise the implemented interpreter features.

To run this file through the interpreter and save the interpreter's output to `test_output.txt`, 
first build the executable:
```bash
./gradlew installDist
```

Then run:
```bash
build/install/lox-interpreter/bin/lox-interpreter src/test/resources/input/test_input.lox > src/test/resources/output/test_output.txt
```

The sample test uses the following files:
- `test_input.lox` - Sample Lox source code that is run through the interpreter.
- `test_output.txt` - Actual output produced by the interpreter.
- `expected_test_output.txt` - Expected output for the sample Lox program.

Compare the generated output with the expected output:
```bash
diff src/test/resources/output/expected_test_output.txt src/test/resources/output/test_output.txt
```
If `diff` produces no output, the actual and expected outputs match.

## Running the Interpreter
### Run from the Command Line
Start the Lox interpreter with:
```bash
./gradlew run --console=plain
```
Enter Lox statements into the interactive prompt. Press `Ctrl+D` to exit.

### Run a Lox Source File
To run any `.lox` source file:
```bash
./gradlew run --args="path/to/file.lox"
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
    │       │   ├── LoxCallable.kt          # Defines callable Lox values
    │       │   ├── LoxFunction.kt          # Represents user-defined Lox functions
    │       │   ├── Parser.kt               # Parses tokens into expression and statement trees
    │       │   ├── Resolver.kt             # Resolves lexical scope for local variables
    │       │   ├── Return.kt               # Handles values returned from functions
    │       │   ├── RuntimeError.kt         # Represents runtime evaluation errors
    │       │   ├── Scanner.kt              # Scans Lox source code into tokens
    │       │   ├── Stmt.kt                 # AST statement classes
    │       │   ├── Token.kt                # Represents individual tokens
    │       │   └── TokenType.kt            # Defines the different token types
    │       └── tool/
    │           └── GenerateAst.kt          # Generates the AST expression and statement classes
    └── test/
        ├── kotlin/
        │   ├── lox/                        # Tests for the interpreter
        │   │   ├── AstPrinterTest.kt
        │   │   ├── EnvironmentTest.kt
        │   │   ├── ExprTest.kt
        │   │   ├── FunctionTest.kt
        │   │   ├── InterpreterTest.kt
        │   │   ├── LoxIntegrationTest.kt
        │   │   ├── LoxTest.kt
        │   │   ├── ParserTest.kt
        │   │   ├── ResolverTest.kt
        │   │   ├── ScannerTest.kt
        │   │   └── StmtTest.kt
        │   └── tool/
        │       └── GenerateAstTest.kt
        └── resources/                      # Test resources and output
            ├── input/                      # Lox source files used for testing
            │   ├── block_scope.lox
            │   ├── duplicate_local.lox
            │   ├── for.lox
            │   ├── if_else.lox
            │   ├── invalid.lox
            │   ├── logical.lox
            │   ├── own_initializer.lox
            │   ├── resolver_closure.lox
            │   ├── runtime_error.lox
            │   ├── test_input.lox
            │   ├── top_level_return.lox
            │   ├── undefined_variable.lox
            │   ├── variables.lox
            │   └── while.lox
            └── output/                     # Saved test results and expected output
                ├── automated-tests.txt
                ├── expected_test_output.txt
                └── test_output.txt
```