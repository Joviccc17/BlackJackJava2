package hr.algebra.blackjack_dorianjovic.serialization;

import hr.algebra.blackjack_dorianjovic.util.Documented;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Uses the Reflection API to scan classes and generate documentation.
 * Outputs class structure (fields, methods, constructors, annotations)
 * to a text file. Highlights methods annotated with @Documented.
 */
public class ReflectionDocGenerator {

    /**
     * Generates documentation for the given classes and writes it to the specified file.
     *
     * @param outputPath the file path to write documentation to
     * @param classes    the classes to document
     * @throws IOException if writing fails
     */
    public void generateDocumentation(String outputPath, Class<?>... classes) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputPath))) {
            writer.println("=".repeat(80));
            writer.println("  BLACKJACK APPLICATION — AUTO-GENERATED DOCUMENTATION");
            writer.println("  Generated using Java Reflection API");
            writer.println("  Date: " + java.time.LocalDateTime.now());
            writer.println("=".repeat(80));
            writer.println();

            for (Class<?> clazz : classes) {
                documentClass(writer, clazz);
            }

            writer.println("=".repeat(80));
            writer.println("  END OF DOCUMENTATION");
            writer.println("=".repeat(80));
        }
    }

    /**
     * Documents a single class: its modifiers, hierarchy, fields, constructors, and methods.
     */
    private void documentClass(PrintWriter writer, Class<?> clazz) {
        writer.println("-".repeat(80));
        writer.println("CLASS: " + clazz.getName());
        writer.println("-".repeat(80));

        // Class modifiers
        writer.println("  Modifiers: " + Modifier.toString(clazz.getModifiers()));

        // Superclass
        if (clazz.getSuperclass() != null && clazz.getSuperclass() != Object.class) {
            writer.println("  Extends: " + clazz.getSuperclass().getName());
        }

        // Interfaces
        Class<?>[] interfaces = clazz.getInterfaces();
        if (interfaces.length > 0) {
            String ifaceNames = Arrays.stream(interfaces)
                    .map(Class::getSimpleName)
                    .collect(Collectors.joining(", "));
            writer.println("  Implements: " + ifaceNames);
        }

        // Annotations on class
        if (clazz.getAnnotations().length > 0) {
            writer.println("  Annotations: " + Arrays.toString(clazz.getAnnotations()));
        }

        writer.println();

        // Fields
        Field[] fields = clazz.getDeclaredFields();
        if (fields.length > 0) {
            writer.println("  FIELDS (" + fields.length + "):");
            for (Field field : fields) {
                writer.printf("    %s %s %s%n",
                        Modifier.toString(field.getModifiers()),
                        field.getType().getSimpleName(),
                        field.getName());
            }
            writer.println();
        }

        // Constructors
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        if (constructors.length > 0) {
            writer.println("  CONSTRUCTORS (" + constructors.length + "):");
            for (Constructor<?> constructor : constructors) {
                String params = Arrays.stream(constructor.getParameterTypes())
                        .map(Class::getSimpleName)
                        .collect(Collectors.joining(", "));
                writer.printf("    %s %s(%s)%n",
                        Modifier.toString(constructor.getModifiers()),
                        clazz.getSimpleName(),
                        params);
            }
            writer.println();
        }

        // Methods
        Method[] methods = clazz.getDeclaredMethods();
        if (methods.length > 0) {
            writer.println("  METHODS (" + methods.length + "):");
            for (Method method : methods) {
                String params = Arrays.stream(method.getParameterTypes())
                        .map(Class::getSimpleName)
                        .collect(Collectors.joining(", "));
                String modifiers = Modifier.toString(method.getModifiers());
                String returnType = method.getReturnType().getSimpleName();

                writer.printf("    %s %s %s(%s)%n",
                        modifiers, returnType, method.getName(), params);

                // Check for @Documented annotation
                Documented doc = method.getAnnotation(Documented.class);
                if (doc != null) {
                    writer.printf("      ★ @Documented: %s%n", doc.description());
                }
            }
            writer.println();
        }

        // Enum constants
        if (clazz.isEnum()) {
            Object[] constants = clazz.getEnumConstants();
            writer.println("  ENUM CONSTANTS (" + constants.length + "):");
            for (Object constant : constants) {
                writer.println("    " + constant.toString());
            }
            writer.println();
        }

        writer.println();
    }
}

