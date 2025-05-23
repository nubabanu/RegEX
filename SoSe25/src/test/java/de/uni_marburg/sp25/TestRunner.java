package de.uni_marburg.sp25;

import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.engine.discovery.ClassNameFilter;

import java.io.PrintWriter;
import java.util.List;

/**
 * A standalone test runner that runs all tests in the project.
 * This class can be used to run all tests from the command line or IDE.
 */
public class TestRunner {

    public static void main(String[] args) {
        // Create a test discovery request for all tests in the package
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(DiscoverySelectors.selectPackage("de.uni_marburg.sp25"))
                .filters(ClassNameFilter.includeClassNamePatterns(".*Test"))
                .build();

        // Create a test launcher
        Launcher launcher = LauncherFactory.create();

        // Register a listener for test execution events
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        launcher.registerTestExecutionListeners(listener);

        // Execute tests
        System.out.println("Running all tests...");
        launcher.execute(request);

        // Print test summary
        TestExecutionSummary summary = listener.getSummary();
        summary.printTo(new PrintWriter(System.out));

        // Print total counts
        System.out.println("\nTest run completed:");
        System.out.println("Tests found: " + summary.getTestsFoundCount());
        System.out.println("Tests started: " + summary.getTestsStartedCount());
        System.out.println("Tests succeeded: " + summary.getTestsSucceededCount());
        System.out.println("Tests skipped: " + summary.getTestsSkippedCount());
        System.out.println("Tests failed: " + summary.getTestsFailedCount());
        
        // Get and print failed tests
        List<TestExecutionSummary.Failure> failures = summary.getFailures();
        if (!failures.isEmpty()) {
            System.out.println("\nFailed tests:");
            for (TestExecutionSummary.Failure failure : failures) {
                System.out.println("- " + failure.getTestIdentifier().getDisplayName());
                System.out.println("  Error: " + failure.getException().getMessage());
            }
        }
        
        // Set exit code based on test results
        if (summary.getTestsFailedCount() > 0) {
            System.exit(1);
        }
    }
}
