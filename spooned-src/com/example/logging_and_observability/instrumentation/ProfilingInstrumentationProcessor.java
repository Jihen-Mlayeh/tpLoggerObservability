package com.example.logging_and_observability.instrumentation;
import CtBlock;
import CtCodeSnippetStatement;
import java.util.Arrays;
import java.util.List;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;
/**
 * Spoon processor to automatically instrument service methods with profiling logs
 */
public class ProfilingInstrumentationProcessor extends AbstractProcessor<CtMethod<?>> {
    private static final List<String> READ_OPERATIONS = Arrays.asList("getAllProducts", "getProductById");

    private static final List<String> WRITE_OPERATIONS = Arrays.asList("addProduct", "updateProduct", "deleteProduct");

    @Override
    public boolean isToBeProcessed(CtMethod<?> method) {
        // Only process methods in ProductService class
        CtClass<?> parentClass = method.getParent(CtClass.class);
        if (parentClass == null) {
            return false;
        }
        String className = parentClass.getQualifiedName();
        String methodName = method.getSimpleName();
        // Process ProductService methods
        return className.endsWith("ProductService") && (READ_OPERATIONS.contains(methodName) || WRITE_OPERATIONS.contains(methodName));
    }

    @Override
    public void process(CtMethod<?> method) {
        String methodName = method.getSimpleName();
        Factory factory = method.getFactory();
        System.out.println("Processing method: " + methodName);
        // Determine operation type
        String operationType;
        if (READ_OPERATIONS.contains(methodName)) {
            operationType = "READ";
        } else if (WRITE_OPERATIONS.contains(methodName)) {
            operationType = "WRITE";
        } else {
            return;
        }
        // Create the instrumentation code
        String instrumentationCode = generateInstrumentationCode(methodName, operationType);
        // Insert at the beginning of the method
        try {
            CtCodeSnippetStatement snippet = factory.Code().createCodeSnippetStatement(instrumentationCode);
            CtBlock<?> body = method.getBody();
            if (body != null) {
                body.insertBegin(snippet);
                System.out.println("✓ Instrumented method: " + methodName);
            }
        } catch (Exception e) {
            System.err.println("✗ Failed to instrument method: " + methodName);
            e.printStackTrace();
        }
    }

    private String generateInstrumentationCode(String methodName, String operationType) {
        StringBuilder code = new StringBuilder();
        code.append("{\n");
        code.append("    // AUTO-GENERATED PROFILING CODE\n");
        code.append("    try {\n");
        code.append("        if (this.userProfileService != null && this.currentUser != null) {\n");
        // Build the profiling call based on method
        switch (methodName) {
            case "getAllProducts" ->
                {
                    code.append("            this.userProfileService.logOperation(\n");
                    code.append("                this.currentUser,\n");
                    code.append("                \"getAllProducts\",\n");
                    code.append("                com.example.logging_and_observability.profiling.model.UserOperationType.READ,\n");
                    code.append("                null, null, null\n");
                    code.append("            );\n");
                }
            case "getProductById" ->
                {
                    code.append("            this.userProfileService.logOperation(\n");
                    code.append("                this.currentUser,\n");
                    code.append("                \"getProductById\",\n");
                    code.append("                com.example.logging_and_observability.profiling.model.UserOperationType.READ,\n");
                    code.append("                id, null, null\n");
                    code.append("            );\n");
                }
            case "addProduct" ->
                {
                    code.append("            this.userProfileService.logOperation(\n");
                    code.append("                this.currentUser,\n");
                    code.append("                \"addProduct\",\n");
                    code.append("                com.example.logging_and_observability.profiling.model.UserOperationType.WRITE,\n");
                    code.append("                product.getId(), product.getName(), product.getPrice()\n");
                    code.append("            );\n");
                }
            case "updateProduct" ->
                {
                    code.append("            this.userProfileService.logOperation(\n");
                    code.append("                this.currentUser,\n");
                    code.append("                \"updateProduct\",\n");
                    code.append("                com.example.logging_and_observability.profiling.model.UserOperationType.WRITE,\n");
                    code.append("                id, updatedProduct.getName(), updatedProduct.getPrice()\n");
                    code.append("            );\n");
                }
            case "deleteProduct" ->
                {
                    code.append("            this.userProfileService.logOperation(\n");
                    code.append("                this.currentUser,\n");
                    code.append("                \"deleteProduct\",\n");
                    code.append("                com.example.logging_and_observability.profiling.model.UserOperationType.WRITE,\n");
                    code.append("                id, null, null\n");
                    code.append("            );\n");
                }
        }
        code.append("        }\n");
        code.append("    } catch (Exception e) {\n");
        code.append(("        logger.error(\"Profiling error in " + methodName) + "\", e);\n");
        code.append("    }\n");
        code.append("}\n");
        return code.toString();
    }
}