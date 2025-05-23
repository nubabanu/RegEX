package de.uni_marburg.sp25.quality;

import de.uni_marburg.sp25.UserStory;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Analyzes user stories for redundancy
 * User stories are redundant if they describe the same functionality
 */
public class RedundancyAnalyzer extends QualityCriterion {

    public RedundancyAnalyzer(ResourceBundle messages) {
        super("redundancyFree", messages);
    }

    @Override
    public List<QualityProblem> analyze(List<UserStory> userStories) {
        List<QualityProblem> problems = new ArrayList<>();
        if (userStories == null || userStories.size() < 2) {
            return problems;
        }

        // Keep track of pairs that have already been identified for a specific type of redundancy
        List<StoryPairWithType> processedPairs = new ArrayList<>();

        for (int i = 0; i < userStories.size(); i++) {
            for (int j = i + 1; j < userStories.size(); j++) {
                UserStory story1 = userStories.get(i);
                UserStory story2 = userStories.get(j);

                // Check for redundancy by goal
                if (isRedundantByGoal(story1, story2)) {
                    StoryPairWithType currentGoalPair = new StoryPairWithType(story1, story2, "REDUNDANT_GOAL");
                    if (!processedPairs.contains(currentGoalPair)) {
                        problems.add(new QualityProblem(
                                getCriterionName(),
                                messages.getString("quality.problem.redundantGoal"),
                                List.of(story1, story2),
                                "REDUNDANT_GOAL"
                        ));
                        processedPairs.add(currentGoalPair);
                    }
                } 
                // Only check for benefit redundancy if not already found by goal for this pair
                else if (isRedundantByBenefit(story1, story2)) {
                    StoryPairWithType currentBenefitPair = new StoryPairWithType(story1, story2, "REDUNDANT_BENEFIT");
                    if (!processedPairs.contains(currentBenefitPair)) {
                        problems.add(new QualityProblem(
                                getCriterionName(),
                                messages.getString("quality.problem.redundantBenefit"),
                                List.of(story1, story2),
                                "REDUNDANT_BENEFIT"
                        ));
                        processedPairs.add(currentBenefitPair);
                    }
                }
            }
        }
        return problems;
    }

    // Renamed areRedundant to isRedundantByGoal for clarity
    private boolean isRedundantByGoal(UserStory story1, UserStory story2) {
        // Wohlgeformtheit and Atomarität are prerequisites according to the document for goal redundancy.
        // This check is simplified here as those analyses are separate.
        // Assuming stories are well-formed and atomic for this specific check.

        // Check if personas are the same (or similar enough, document says "ihre Rolle")
        // The original implementation checked for exact persona match.
        // if (!isSamePersona(story1, story2)) {
        //     return false;
        // }
        // The document example for goal redundancy does not mention role:
        // "As a Visitor, I want to be able to see a list of news items..."
        // "As a Visitor, I want to be able to view a list of news items..."
        // These have the same role. Let's keep a persona check.
         if (!isSameOrSimilarPersona(story1, story2)) {
            return false;
        }

        return isSimilarAction(story1.getActionGoal(), story2.getActionGoal()) &&
               isSimilarEntity(story1.getEntityGoal(), story2.getEntityGoal());
    }

    private boolean isRedundantByBenefit(UserStory story1, UserStory story2) {
        // Uniformity is a prerequisite for benefit redundancy.
        // Assuming stories are uniform for this specific check.

        // The document example for benefit redundancy has the same role:
        // "As an Adminstrator, I want to create a new user account, so that I am able to manage the users of the system."
        // "As an Adminstrator, I want to edit the permission rights of a user account, so that I am able to manage the users of the system."
        // Let's keep a persona check here too for consistency, though the rule focuses on benefit text.
        if (!isSameOrSimilarPersona(story1, story2)) {
            return false;
        }

        String benefit1 = story1.getBenefit();
        String benefit2 = story2.getBenefit();

        if (benefit1 == null || benefit2 == null || benefit1.trim().isEmpty() || benefit2.trim().isEmpty()) {
            return false; // Cannot compare if benefits are missing
        }
        // Simple equality or similarity check for benefits
        return benefit1.equalsIgnoreCase(benefit2) || areSentencesSimilar(benefit1, benefit2);
    }
    
    private boolean isSameOrSimilarPersona(UserStory story1, UserStory story2) {
        List<String> personaList1 = story1.getPersona();
        List<String> personaList2 = story2.getPersona();

        if (personaList1 == null || personaList1.isEmpty() || personaList2 == null || personaList2.isEmpty()) {
            return false; // Cannot compare if personas are missing
        }
        // For simplicity, comparing the first persona. A more robust check might compare all.
        return personaList1.get(0).equalsIgnoreCase(personaList2.get(0));
    }

    private boolean isSimilarAction(List<String> actions1, List<String> actions2) {
        if (actions1 == null || actions1.isEmpty() || actions2 == null || actions2.isEmpty()) {
            return false;
        }
        // Assuming atomicity, so one primary action.
        String action1 = actions1.get(0).toLowerCase();
        String action2 = actions2.get(0).toLowerCase();

        return action1.equals(action2) || areSynonyms(action1, action2);
    }

    private boolean isSimilarEntity(List<String> entities1, List<String> entities2) {
        if (entities1 == null || entities1.isEmpty() || entities2 == null || entities2.isEmpty()) {
            return false;
        }
        // Assuming one primary entity for simplicity in comparison.
        String entity1 = entities1.get(0).toLowerCase();
        String entity2 = entities2.get(0).toLowerCase();

        // More robust check: Jaccard index, or other string similarity metrics.
        // For now, using equals or contains.
        return entity1.equals(entity2) || entity1.contains(entity2) || entity2.contains(entity1);
    }

    private boolean areSynonyms(String word1, String word2) {
        // This is a very basic synonym check. A real implementation would use a thesaurus or NLP library.
        if ((word1.equals("view") || word1.equals("see") || word1.equals("display")) &&
            (word2.equals("view") || word2.equals("see") || word2.equals("display"))) {
            return true;
        }
        if ((word1.equals("create") || word1.equals("add") || word1.equals("generate")) &&
            (word2.equals("create") || word2.equals("add") || word2.equals("generate"))) {
            return true;
        }
        if ((word1.equals("edit") || word1.equals("modify") || word1.equals("update")) &&
            (word2.equals("edit") || word2.equals("modify") || word2.equals("update"))) {
            return true;
        }
        // Add more synonym pairs as needed
        return false;
    }

    private boolean areSentencesSimilar(String sentence1, String sentence2) {
        // Placeholder for a more sophisticated sentence similarity check (e.g., using embeddings, Jaccard on words, etc.)
        // For now, a simple check after normalizing.
        String normalized1 = sentence1.toLowerCase().replaceAll("[^a-z0-9\\s]", "").trim();
        String normalized2 = sentence2.toLowerCase().replaceAll("[^a-z0-9\\s]", "").trim();
        return normalized1.equals(normalized2);
    }

    // Helper record for story pairs with redundancy type
    private record StoryPairWithType(UserStory story1, UserStory story2, String type) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            StoryPairWithType that = (StoryPairWithType) o;
            // Order of stories in a pair does not matter for equality
            return type.equals(that.type) &&
                   ((story1.equals(that.story1) && story2.equals(that.story2)) ||
                    (story1.equals(that.story2) && story2.equals(that.story1)));
        }

        @Override
        public int hashCode() {
            // Order-agnostic hashcode for stories, combined with type
            int storyHash = story1.hashCode() ^ story2.hashCode();
            // Combine with type hashcode using a prime multiplier
            return 31 * storyHash + type.hashCode();
        }
    }
}
