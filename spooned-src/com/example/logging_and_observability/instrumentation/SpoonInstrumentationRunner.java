package com.example.logging_and_observability.instrumentation;
import java.io.File;
import spoon.Launcher;
/**
 * Runner to execute Spoon instrumentation on ProductService
 */
public class SpoonInstrumentationRunner {
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════╗");
        System.out.println("║   SPOON AUTOMATIC CODE INSTRUMENTATION         ║");
        System.out.println("╚════════════════════════════════════════════════╝\n");
        try {
            // Create Spoon launcher
            Launcher launcher = new Launcher();
            // Configure input (source code to analyze)
            String sourceDir = "src/main/java";
            if (new File(sourceDir).exists()) {
                launcher.addInputResource(sourceDir);
                System.out.println("✓ Source directory added: " + sourceDir);
            } else {
                System.err.println("✗ Source directory not found: " + sourceDir);
                System.err.println("  Please run this from your project root directory");
                return;
            }
            // Configure output
            String outputDir = "spooned-src";
            launcher.setSourceOutputDirectory(outputDir);
            System.out.println("✓ Output directory set: " + outputDir);
            // Add processor
            launcher.addProcessor(new ProfilingInstrumentationProcessor());
            System.out.println("✓ Profiling processor added\n");
            // Set environment options
            launcher.getEnvironment().setAutoImports(true);
            launcher.getEnvironment().setCommentEnabled(true);
            launcher.getEnvironment().setNoClasspath(true);
            launcher.getEnvironment().setComplianceLevel(17);
            System.out.println("Starting code analysis and instrumentation...\n");
            // Run the instrumentation
            launcher.run();
            System.out.println("\n╔════════════════════════════════════════════════╗");
            System.out.println("║   INSTRUMENTATION COMPLETED SUCCESSFULLY!      ║");
            System.out.println("╚════════════════════════════════════════════════╝");
            System.out.println("\nInstrumented code saved to: " + outputDir);
            System.out.println("\nNext steps:");
            System.out.println("1. Review the instrumented code in: " + outputDir);
            System.out.println("2. Copy the instrumented ProductService.java back to src/main/java");
            System.out.println("3. Update ProductService to include UserProfileService dependency");
            System.out.println("4. Run your application and observe the profiling!");
        } catch (Exception e) {
            System.err.println("\n✗ Instrumentation failed!");
            e.printStackTrace();
        }
    }
}