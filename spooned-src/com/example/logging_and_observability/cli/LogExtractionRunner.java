package com.example.logging_and_observability.cli;
import com.example.logging_and_observability.logging.extractor.ProfileExtractor;
import com.example.logging_and_observability.logging.model.LogEntry;
import com.example.logging_and_observability.logging.parser.LogParser;
import com.example.logging_and_observability.profiling.model.UserProfile;
import java.util.List;
import java.util.Map;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
/**
 * CLI runner for parsing logs and extracting profiles (Question 5)
 *
 * Usage:
 * mvn spring-boot:run -Dspring-boot.run.arguments="--extract-profiles"
 */
// Run before other runners
@Slf4j
@Component
@Order(0)
@RequiredArgsConstructor
public class LogExtractionRunner implements CommandLineRunner {
    private final LogParser logParser;

    private final ProfileExtractor profileExtractor;

    @Override
    public void run(String... args) throws Exception {
        // Check if we should extract profiles
        boolean extractProfiles = false;
        for (String arg : args) {
            if ("--extract-profiles".equals(arg) || "--parse-logs".equals(arg)) {
                extractProfiles = true;
                break;
            }
        }
        if (extractProfiles) {
            log.info("\n");
            log.info("╔════════════════════════════════════════════════════════╗");
            log.info("║   📊 STARTING LOG PARSING & PROFILE EXTRACTION         ║");
            log.info("║   Question 5: Parse logs and construct user profiles  ║");
            log.info("╚════════════════════════════════════════════════════════╝");
            log.info("\n");
            try {
                // Step 1: Parse logs
                log.info("Step 1: Parsing log files...");
                List<LogEntry> logs = logParser.parseDefaultLogs();
                log.info("✅ Parsed {} log entries\n", logs.size());
                // Step 2: Extract profiles
                log.info("Step 2: Extracting user profiles from logs...");
                Map<String, UserProfile> profiles = profileExtractor.extractProfilesFromLogs();
                log.info("✅ Extracted {} user profiles\n", profiles.size());
                // Step 3: Export profiles
                log.info("Step 3: Exporting profiles to JSON...");
                profileExtractor.exportProfiles(profiles);
                log.info("✅ Profiles exported\n");
                // Step 4: Generate report
                String report = profileExtractor.generateExtractionReport(profiles);
                log.info(report);
                log.info("\n");
                log.info("╔════════════════════════════════════════════════════════╗");
                log.info("║   ✅ LOG EXTRACTION COMPLETED SUCCESSFULLY!            ║");
                log.info("╚════════════════════════════════════════════════════════╝");
                log.info("\n");
                log.info("📁 Check extracted-profiles/ directory for results");
                log.info("📊 Check structured-logs/ directory for structured logs");
                log.info("\n");
            } catch (Exception e) {
                log.error("❌ Profile extraction failed", e);
            }
            // Exit after extraction
            log.info("✅ Exiting application...");
            System.exit(0);
        }
    }
}