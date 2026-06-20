package hr.algebra.blackjack_dorianjovic.serialization;

import hr.algebra.blackjack_dorianjovic.util.Documented;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ReflectionDocGenerator {

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

    private void documentClass(PrintWriter writer, Class<?> clazz) {
        writer.println("-".repeat(80));
        writer.println("CLASS: " + clazz.getName());
        writer.println("-".repeat(80));

        writer.println("  Modifiers: " + Modifier.toString(clazz.getModifiers()));

        if (clazz.getSuperclass() != null && clazz.getSuperclass() != Object.class) {
            writer.println("  Extends: " + clazz.getSuperclass().getName());
        }

        Class<?>[] interfaces = clazz.getInterfaces();
        if (interfaces.length > 0) {
            String ifaceNames = Arrays.stream(interfaces)
                    .map(Class::getSimpleName)
                    .collect(Collectors.joining(", "));
            writer.println("  Implements: " + ifaceNames);
        }

        if (clazz.getAnnotations().length > 0) {
            writer.println("  Annotations: " + Arrays.toString(clazz.getAnnotations()));
        }

        writer.println();

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

                Documented doc = method.getAnnotation(Documented.class);
                if (doc != null) {
                    writer.printf("      ★ @Documented: %s%n", doc.description());
                }
            }
            writer.println();
        }

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
