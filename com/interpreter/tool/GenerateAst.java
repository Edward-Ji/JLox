package com.interpreter.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {

    static final String indentString = "\t";
    static int indentCount = 0;
    static PrintWriter writer;

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: generate_ast <output directory>");
            System.exit(64);
        }
        String outputDir = args[0];

        defineAst(outputDir, "Expr", Arrays.asList(
            "Binary   : Expr left, Token operator, Expr right",
            "Grouping : Expr expression",
            "Literal  : Object value",
            "Unary    : Token operator, Expr right"
        ));
    }

    private static void defineAst(
        String outputDir, String baseName, List<String> types
    ) throws IOException {
        String path = outputDir + "/" + baseName + ".java";

        writer = new PrintWriter(path, "UTF-8");

        writeln("package com.interpreter.jlox;");
        writeln();
        writeln("import java.util.List;");
        writeln();
        writeln("abstract class " + baseName + " {");
        indent();

        defineVisitor(baseName, types);

        // The AST classes.
        for (String type : types) {
            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();
            defineType(baseName, className, fields);
        }

        // The base accept() method.
        writeln();
        writeln("abstract <R> R accept(Visitor<R> visitor);");

        unindent();
        writeln("}");
        writer.close();
    }

    private static void defineVisitor(
        String baseName, List<String> types
    ) {
        writeln();
        writeln("interface Visitor<R> {");
        indent();

        for (String type : types) {
            String typeName = type.split(":")[0].trim();
            writeln("R visit" + typeName + baseName + "(" + typeName + " "
                + baseName.toLowerCase() + ");");
        }

        unindent();
        writeln("}");
    }

    private static void defineType(
        String baseName, String className, String fieldList
    ) {
        writeln();
        writeln("static class " + className + " extends " + baseName + " {");
        indent();

        // Constructor.
        writeln(className + "(" + fieldList + ") {");
        indent();

        // Store parameters in fields.
        String[] fields = fieldList.split(", ");
        for (String field : fields) {
            String name = field.split(" ")[1];
            writeln("this." + name + " = " + name + ";");
        }

        unindent();
        writeln("}");

        // Visitor pattern.
        writeln();
        writeln("@Override");
        writeln("<R> R accept(Visitor<R> visitor) {");
        indent();
        writeln("return visitor.visit" + className + baseName + "(this);");
        unindent();
        writeln("}");

        // Fields.
        writeln();
        for (String field : fields) {
            writeln("final " + field + ";");
        }

        unindent();
        writeln("}");
    }

    private static void indent() {
        indentCount += 1;
    }

    private static void unindent() {
        indentCount -= 1;
    }

    private static void writeln() {
        writer.println();
    }

    private static void writeln(String string) {
        writer.println(indentString.repeat(indentCount) + string);
    }
}
