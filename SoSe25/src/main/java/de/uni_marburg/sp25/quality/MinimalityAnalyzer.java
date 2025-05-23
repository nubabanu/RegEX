package de.uni_marburg.sp25.quality;

import de.uni_marburg.sp25.UserStory;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Analyzes user stories for minimality
 * A user story is minimal if it contains only essential information (role, goal, benefit)
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

    private boolean isMinimal(UserStory story) {
        if (story.getText() == null || story.getText().trim().isEmpty()) {
            return true; 
        }

        String originalText = story.getText().toLowerCase();
        // Clean the original text: remove punctuation (except within words like em-dashes if needed later, but for now general clean), normalize spaces.
        String cleanedOriginalText = originalText.replaceAll("[^a-z0-9\\s\\p{Pd}]", "").replaceAll("\\s+", " ").trim(); // Keep hyphens/dashes
        String remainingText = cleanedOriginalText;

        // 1. Remove Persona
        if (story.getPersona() != null && !story.getPersona().isEmpty()) {
            String personaPhrase = String.join(" ", story.getPersona()).toLowerCase()
                                   .replaceAll("[^a-z0-9\\s\\p{Pd}]", "").replaceAll("\\s+", " ").trim();
            remainingText = remainingText.replaceFirst("as a " + personaPhrase, "").trim();
            remainingText = remainingText.replaceFirst("^,\\s*", "").trim(); // Clean leading comma from removal
        }

        // 2. Remove "I want to [ActionGoal]" or "I want [ActionGoal]"
        if (story.getActionGoal() != null && !story.getActionGoal().isEmpty()) {
            String actionPhrase = String.join(" ", story.getActionGoal()).toLowerCase()
                                  .replaceAll("[^a-z0-9\\s\\p{Pd}]", "").replaceAll("\\s+", " ").trim();
            if (!actionPhrase.isEmpty()) {
                remainingText = remainingText.replaceFirst("i want to " + actionPhrase, "").trim();
                remainingText = remainingText.replaceFirst("i want " + actionPhrase, "").trim(); // Variation without "to"
                remainingText = remainingText.replaceFirst("^,\\s*", "").trim();
            }
        }

        // 3. Remove "so that [Benefit]" or just "[Benefit]" if "so that" is absent
        if (story.getBenefit() != null && !story.getBenefit().trim().isEmpty()) {
            String benefitText = story.getBenefit().toLowerCase()
                                 .replaceAll("[^a-z0-9\\s\\p{Pd}]", "").replaceAll("\\s+", " ").trim();
            if (!benefitText.isEmpty()) {
                remainingText = remainingText.replaceFirst("so that " + benefitText, "").trim();
                remainingText = remainingText.replaceFirst(benefitText, "").trim(); 
                remainingText = remainingText.replaceFirst("^,\\s*", "").trim();
            }
        }
        
        // General cleanup of common structural remnants and leading/trailing punctuation
        remainingText = remainingText.replaceAll("^(,|so that|i want to|i want|as a)\\s*", "").trim();
        remainingText = remainingText.replaceAll("\\s*(,|so that)$", "").trim();
        remainingText = remainingText.replaceAll("\\p{Punct}$", "").trim(); // Trailing punctuation

        // If remainingText is empty at this point, it's minimal.
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
