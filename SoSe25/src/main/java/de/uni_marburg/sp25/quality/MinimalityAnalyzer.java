package de.uni_marburg.sp25.quality;

import de.uni_marburg.sp25.UserStory;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Analyzes user stories for minimality - ensuring they contain only essential information.
 * 
 * The minimality principle states that a user story should contain only the essential
 * components: role (persona), goal (action), and benefit. Any extraneous information
 * makes the story unnecessarily complex and harder to understand or implement.
 * 
 * Analysis Process:
 * 1. Extract the raw user story text
 * 2. Remove identified persona, action goal, and benefit components
 * 3. Clean up structural phrases ("as a", "I want to", "so that")
 * 4. Check if any significant content remains after component removal
 * 5. Flag stories with remaining content as non-minimal
 * 
 * This analyzer helps identify stories that might need refactoring to focus
 * on core functionality without unnecessary details or multiple concerns.
 */
public class MinimalityAnalyzer extends QualityCriterion {

    public MinimalityAnalyzer(ResourceBundle messages) {
        super("minimality", messages);
    }

    @Override
    public List<QualityProblem> analyze(List<UserStory> userStories) {
        List<QualityProblem> problems = new ArrayList<>();

        for (UserStory story : userStories) {
            if (!isMinimal(story)) {
                problems.add(new QualityProblem(
                    criterionName,
                    messages.getString("quality.problem.notMinimal"),
                    List.of(story),
                    "NOT_MINIMAL"
                ));
            }
        }
        return problems;
    }

    /**
     * Determines if a user story is minimal by removing known components and checking for remaining content.
     * 
     * @param story The user story to analyze
     * @return true if the story contains only essential components, false if extra content exists
     */
    private boolean isMinimal(UserStory story) {
        if (story.getText() == null || story.getText().trim().isEmpty()) {
            return true; 
        }

        String originalText = story.getText().toLowerCase();
        // Normalize text: remove punctuation, normalize spaces, keep hyphens for compound terms
        String cleanedOriginalText = originalText.replaceAll("[^a-z0-9\\s\\p{Pd}]", "").replaceAll("\\s+", " ").trim();
        String remainingText = cleanedOriginalText;

        // Remove persona component if present
        if (story.getPersona() != null && !story.getPersona().isEmpty()) {
            String personaPhrase = String.join(" ", story.getPersona()).toLowerCase()
                                   .replaceAll("[^a-z0-9\\s\\p{Pd}]", "").replaceAll("\\s+", " ").trim();
            remainingText = remainingText.replaceFirst("as a " + personaPhrase, "").trim();
            remainingText = remainingText.replaceFirst("^,\\s*", "").trim();
        }

        // Remove action goal component variations
        if (story.getActionGoal() != null && !story.getActionGoal().isEmpty()) {
            String actionPhrase = String.join(" ", story.getActionGoal()).toLowerCase()
                                  .replaceAll("[^a-z0-9\\s\\p{Pd}]", "").replaceAll("\\s+", " ").trim();
            if (!actionPhrase.isEmpty()) {
                remainingText = remainingText.replaceFirst("i want to " + actionPhrase, "").trim();
                remainingText = remainingText.replaceFirst("i want " + actionPhrase, "").trim();
                remainingText = remainingText.replaceFirst("^,\\s*", "").trim();
            }
        }

        // Remove benefit component 
        if (story.getBenefit() != null && !story.getBenefit().trim().isEmpty()) {
            String benefitText = story.getBenefit().toLowerCase()
                                 .replaceAll("[^a-z0-9\\s\\p{Pd}]", "").replaceAll("\\s+", " ").trim();
            if (!benefitText.isEmpty()) {
                remainingText = remainingText.replaceFirst("so that " + benefitText, "").trim();
                remainingText = remainingText.replaceFirst(benefitText, "").trim(); 
                remainingText = remainingText.replaceFirst("^,\\s*", "").trim();
            }
        }
        
        // Clean up structural remnants and punctuation
        remainingText = remainingText.replaceAll("^(,|so that|i want to|i want|as a)\\s*", "").trim();
        remainingText = remainingText.replaceAll("\\s*(,|so that)$", "").trim();
        remainingText = remainingText.replaceAll("\\p{Punct}$", "").trim();

        // Story is minimal if no significant content remains after component removal
        if (remainingText.isEmpty()) {
            return true;
        }

        // Non-minimal keywords/phrases check
        String[] nonMinimalKeywords = {"note", "mockup", "screen", "validation", "technical", "implementation", "split into"}; // "split into" for cases like "(split into products and activities)"
        for (String keyword : nonMinimalKeywords) {
            if (remainingText.contains(keyword)) {
                return false;
            }
        }
        if (remainingText.matches(".*(this feature is|is high priority|is low priority|is medium priority|high priority|low priority|medium priority).*")) {
            return false;
        }
        if (remainingText.matches(".*(see:|see mockup from|see also|refer to|example:|e\\.g\\.).*")) {
            return false;
        }

        // If remainingText is exactly the (cleaned) entityGoal, it's considered part of the core story.
        if (story.getEntityGoal() != null && !story.getEntityGoal().isEmpty()) {
            String entityPhraseCleaned = String.join(" ", story.getEntityGoal()).toLowerCase()
                                           .replaceAll("[^a-z0-9\\s\\p{Pd}]", "").replaceAll("\\s+", " ").trim();
            if (remainingText.equals(entityPhraseCleaned)) {
                return true; 
            }
        }
        
        // If remainingText is still not empty after all checks, it implies extra information.
        if (!remainingText.isEmpty()) {
            return false; 
        }

        return true; // Should be covered by earlier checks, but as a fallback.
    }
}
