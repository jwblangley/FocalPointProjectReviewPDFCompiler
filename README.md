# FocalPoint Project Review PDF Compiler

Utility to compile FocalPoint project review documents

This project was forked from [jwblangley/PDFSuffixInterleaver](https://github.com/jwblangley/PDFSuffixInterleaver)

## Building

This application uses gradle to create an *uber jar*: a portable java application with dependencies included.

* `java` 21 is required
* `gradle` >=8.0 is required

```bash
gradle shadowJar
```

## Running

The build output is found at `./build/libs/FocalPointCompiler.jar`.
This jar should be executable and simply run with double click on most systems.

For CLI (recommeded for development):

```bash
java -jar ./build/libs/FocalPointCompiler.jar
```

## Contributing

This application is designed using the [Model-View-Controller architecture](https://en.wikipedia.org/wiki/Model%E2%80%93view%E2%80%93controller), please keep to this architecture for future developments
