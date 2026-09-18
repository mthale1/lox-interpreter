# Lox Interpreter

A Lox interpreter implemented in Kotlin based on *Crafting Interpreters*.

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

**Note:** At the Chapter 4 stage, the interpreter scans and prints tokens but does not yet execute the Lox program.
