package com.example.logging_and_observability.cli;

import com.example.logging_and_observability.scenario.ScenarioExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Runner to execute automated scenarios for Question 4
 *
 * USAGE:
 * mvn spring-boot:run -Dspring-boot.run.arguments="--run-scenarios"
 */
@Slf4j
@Component
@Order(1) // Run before ProductCLI
@RequiredArgsConstructor
public class ScenarioRunner implements CommandLineRunner {

    private final ScenarioExecutor scenarioExecutor;

    @Override
    public void run(String... args) throws Exception {
        // Check if we should run scenarios
        boolean runScenarios = false;

        // Check command line arguments
        for (String arg : args) {
            if ("--run-scenarios".equals(arg) || "--scenarios".equals(arg)) {
                runScenarios = true;
                break;
            }
        }

        if (runScenarios) {
            log.info("\n");
            log.info("╔════════════════════════════════════════════════════════╗");
            log.info("║   🎬 STARTING AUTOMATED SCENARIO EXECUTION             ║");
            log.info("║   Creating 10 users with diverse operation patterns   ║");
            log.info("╚════════════════════════════════════════════════════════╝");
            log.info("\n");

            try {
                scenarioExecutor.executeAllScenarios();

                log.info("\n");
                log.info("╔════════════════════════════════════════════════════════╗");
                log.info("║   ✅ SCENARIOS COMPLETED SUCCESSFULLY!                 ║");
                log.info("╚════════════════════════════════════════════════════════╝");
                log.info("\n");
                log.info("📁 Check user-profiles/ directory for generated profiles");
                log.info("🗄️  Check MongoDB for users and products collections");
                log.info("\n");

            } catch (Exception e) {
                log.error("❌ Scenario execution failed", e);
            }

            // Exit after scenarios
            log.info("✅ Exiting application...");
            System.exit(0);
        }
    }
}