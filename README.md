# Lox Interpreter 
A Lox interpreter implemented in Kotlin based on the interpreter developed in *Crafting Interpreters*.

**Name:** Makenzie Hale  
**Programming Language:** Kotlin  

## Project Summary
This project implements an interpreter for the Lox programming language in Kotlin, based on Crafting Interpreters. 
The interpreter can read and execute Lox programs with variables, expressions, control flow, functions, closures, 
classes, methods, and inheritance. It also handles lexical scope and reports syntax, resolution, and runtime errors. 
The project includes the functionality implemented through Chapter 13.

### Limitations 
No known deficiencies.
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
- `ScannerTest.kt` - Tests scanning and tokenization.
- `LoxTest.kt` - Tests scanning/parsing integration, AST output, and error reporting.
- `ExprTest.kt` - Tests AST expression classes and Visitor behavior.
- `StmtTest.kt` - Tests AST statement classes and Visitor behavior.
- `AstPrinterTest.kt` - Tests AST expression printing.
- `EnvironmentTest.kt` - Tests variable environments and scope access.
- `FunctionTest.kt` - Tests user-defined function behavior.
- `LoxClassTest.kt` - Tests Lox class behavior.
- `LoxInstanceTest.kt` - Tests Lox instance behavior.
- `GenerateAstTest.kt` - Tests AST code generation.
- `ParserTest.kt` - Tests parsing expressions and statements.
- `ResolverTest.kt` - Tests lexical scope resolution and resolution errors.
- `InterpreterTest.kt` - Tests expression and statement interpretation.
- `LoxIntegrationTest.kt` - Tests the complete interpreter pipeline and command-line behavior.

### Sample Input and Output
`src/test/resources/input/test_input.lox` is a sample Lox program included with the project.
It contains Lox statements that exercise the implemented interpreter features through Chapter 13.

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
    │       │   ├── LoxClass.kt             # Represents Lox classes and class methods
    │       │   ├── LoxFunction.kt          # Represents user-defined Lox functions
    │       │   ├── LoxInstance.kt          # Represents class instances and instance fields
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
        │   │   ├── LoxClassTest.kt
        │   │   ├── LoxInstanceTest.kt
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
            │   ├── classes.lox
            │   ├── duplicate_local.lox
            │   ├── for.lox
            │   ├── if_else.lox
            │   ├── inheritance.lox
            │   ├── initializer_return_value.lox
            │   ├── invalid.lox
            │   ├── invalid_superclass.lox
            │   ├── logical.lox
            │   ├── own_initializer.lox
            │   ├── resolver_closure.lox
            │   ├── runtime_error.lox
            │   ├── self_inheritance.lox
            │   ├── super_outside_class.lox
            │   ├── super_without_superclass.lox
            │   ├── test_input.lox
            │   ├── this_outside_class.lox
            │   ├── top_level_return.lox
            │   ├── undefined_property.lox
            │   ├── undefined_super_method.lox
            │   ├── undefined_variable.lox
            │   ├── variables.lox
            │   └── while.lox
            └── output/                     # Saved test results and expected output
                ├── automated-tests.txt
                ├── expected_test_output.txt
                └── test_output.txt
```