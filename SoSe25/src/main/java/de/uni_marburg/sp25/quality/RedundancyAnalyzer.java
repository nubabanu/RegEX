package de.uni_marburg.sp25.quality;

import de.uni_marburg.sp25.UserStory;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Analyzes user stories for redundancy according to the INVEST criteria.
 * 
 * User stories are considered redundant if they describe essentially the same functionality
 * through similar goals or identical benefits. This analyzer identifies two types of redundancy:
 * 
 * 1. Goal Redundancy: Stories with the same persona that have similar actions on similar entities
 *    Example: "As a Visitor, I want to see a list of news items" vs 
 *             "As a Visitor, I want to view a list of news items"
 * 
 * 2. Benefit Redundancy: Stories with identical or highly similar benefit statements
 *    Example: "...so that I am able to manage the users of the system" (repeated across stories)
 * 
 * The analyzer uses natural language processing techniques including:
 * - Synonym detection for actions (view/see/display, create/add/generate, edit/modify/update)
 * - Entity similarity checking through string containment and equality
 * - Sentence normalization for benefit comparison
 * - Persona matching to ensure stories refer to the same user type
 * 
 * Dependencies:
 * - ResourceBundle: For internationalized error messages
 * - Extends QualityCriterion: Provides base functionality for quality analysis
 * - Integrates with UserStory components: persona, actionGoal, entityGoal, benefit
 */
public class RedundancyAnalyzer extends QualityCriterion {

    /**
     * Constructs a RedundancyAnalyzer with internationalized messages.
     * 
     * @param messages ResourceBundle containing localized error messages and descriptions
     */
    public RedundancyAnalyzer(ResourceBundle messages) {
        super("redundancyFree", messages);
    }

    /**
     * Analyzes a collection of user stories for redundancy violations.
     * 
     * Performs pairwise comparison of all stories to detect redundancy through:
     * - Goal-based redundancy: Same persona with similar actions on similar entities
     * - Benefit-based redundancy: Stories with identical or highly similar benefits
     * 
     * Uses deduplication logic to prevent reporting the same pair multiple times
     * and prioritizes goal redundancy over benefit redundancy for clarity.
     * 
     * @param userStories List of UserStory objects to analyze
     * @return List of QualityProblem objects identifying redundant story pairs
     */
    @Override
    public List<QualityProblem> analyze(List<UserStory> userStories) {
        List<QualityProblem> problems = new ArrayList<>();
        if (userStories == null || userStories.size() < 2) {
            return problems; // Cannot have redundancy with fewer than 2 stories
        }

        // Track processed pairs to prevent duplicate problem reporting
        List<StoryPairWithType> processedPairs = new ArrayList<>();

        for (int i = 0; i < userStories.size(); i++) {
            for (int j = i + 1; j < userStories.size(); j++) {
                UserStory story1 = userStories.get(i);
                UserStory story2 = userStories.get(j);

                // Check for goal-based redundancy first (higher priority)
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
                // Check for benefit redundancy only if goal redundancy not found
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

    /**
     * Determines if two user stories are redundant based on their goals.
     * 
     * Goal redundancy occurs when stories have:
     * - Same or similar persona (user role)
     * - Similar actions (using synonym detection)
     * - Similar entities (target objects of the action)
     * 
     * Prerequisites: Stories should be well-formed and atomic for accurate comparison.
     * 
     * @param story1 First user story to compare
     * @param story2 Second user story to compare
     * @return true if stories are redundant by goal, false otherwise
     */
    private boolean isRedundantByGoal(UserStory story1, UserStory story2) {
        // Verify personas match (same user role)
         if (!isSameOrSimilarPersona(story1, story2)) {
            return false;
        }

        // Compare actions and entities using similarity algorithms
        return isSimilarAction(story1.getActionGoal(), story2.getActionGoal()) &&
               isSimilarEntity(story1.getEntityGoal(), story2.getEntityGoal());
    }

    /**
     * Determines if two user stories are redundant based on their benefits.
     * 
     * Benefit redundancy occurs when stories have:
     * - Same or similar persona (for consistency)
     * - Identical or highly similar benefit statements
     * 
     * Prerequisites: Stories should be uniform (have benefit clauses) for accurate comparison.
     * 
     * @param story1 First user story to compare
     * @param story2 Second user story to compare
     * @return true if stories are redundant by benefit, false otherwise
     */
    private boolean isRedundantByBenefit(UserStory story1, UserStory story2) {
        // Verify personas match for consistency
        if (!isSameOrSimilarPersona(story1, story2)) {
            return false;
        }

        String benefit1 = story1.getBenefit();
        String benefit2 = story2.getBenefit();

                if (benefit1 == null || benefit2 == null || benefit1.trim().isEmpty() || benefit2.trim().isEmpty()) {
            return false; // Cannot compare benefits if they are missing or empty
        }
        
        // Use both exact equality and similarity algorithms for comprehensive comparison
        return benefit1.equalsIgnoreCase(benefit2) || areSentencesSimilar(benefit1, benefit2);
    }
    
    /**
     * Checks if two user stories have the same or similar personas (user roles).
     * 
     * Compares the first persona from each story's persona list using case-insensitive matching.
     * A more robust implementation could compare all personas or use fuzzy matching.
     * 
     * @param story1 First user story to compare
     * @param story2 Second user story to compare
     * @return true if personas are the same or similar, false otherwise
     */
    private boolean isSameOrSimilarPersona(UserStory story1, UserStory story2) {
        List<String> personaList1 = story1.getPersona();
        List<String> personaList2 = story2.getPersona();

        if (personaList1 == null || personaList1.isEmpty() || personaList2 == null || personaList2.isEmpty()) {
            return false; // Cannot compare if personas are missing
        }
        
        // Compare primary personas (first in list) with case-insensitive matching
        return personaList1.get(0).equalsIgnoreCase(personaList2.get(0));
    }

    /**
     * Determines if two action lists represent similar functionality.
     * 
     * Uses synonym detection and exact matching to identify similar actions.
     * Assumes atomicity (single primary action) for simplified comparison.
     * 
     * @param actions1 Action list from first user story
     * @param actions2 Action list from second user story
     * @return true if actions are similar or synonymous, false otherwise
     */
    private boolean isSimilarAction(List<String> actions1, List<String> actions2) {
        if (actions1 == null || actions1.isEmpty() || actions2 == null || actions2.isEmpty()) {
            return false;
        }
        
        // Compare primary actions with case normalization
        String action1 = actions1.get(0).toLowerCase();
        String action2 = actions2.get(0).toLowerCase();

        return action1.equals(action2) || areSynonyms(action1, action2);
    }

    /**
     * Determines if two entity lists represent similar target objects.
     * 
     * Uses exact matching and containment checking to identify similar entities.
     * Handles cases where one entity name contains another (e.g., "news item" vs "item").
     * 
     * @param entities1 Entity list from first user story
     * @param entities2 Entity list from second user story
     * @return true if entities are similar or related, false otherwise
     */
    private boolean isSimilarEntity(List<String> entities1, List<String> entities2) {
        if (entities1 == null || entities1.isEmpty() || entities2 == null || entities2.isEmpty()) {
            return false;
        }
        
        // Compare primary entities with case normalization
        String entity1 = entities1.get(0).toLowerCase();
        String entity2 = entities2.get(0).toLowerCase();

        // Check for exact match or containment relationship
        return entity1.equals(entity2) || entity1.contains(entity2) || entity2.contains(entity1);
    }

    /**
     * Checks if two action words are synonymous using a predefined synonym dictionary.
     * 
     * Maintains a basic synonym mapping for common user story actions:
     * - View actions: view, see, display
     * - Creation actions: create, add, generate
     * - Modification actions: edit, modify, update
     * 
     * A production implementation could use a comprehensive thesaurus or NLP library.
     * 
     * @param word1 First action word to compare
     * @param word2 Second action word to compare
     * @return true if words are synonymous, false otherwise
     */
    private boolean areSynonyms(String word1, String word2) {
        // Viewing/displaying synonyms
        if ((word1.equals("view") || word1.equals("see") || word1.equals("display")) &&
            (word2.equals("view") || word2.equals("see") || word2.equals("display"))) {
            return true;
        }
        // Creation synonyms
        if ((word1.equals("create") || word1.equals("add") || word1.equals("generate")) &&
            (word2.equals("create") || word2.equals("add") || word2.equals("generate"))) {
            return true;
        }
        // Modification synonyms
        if ((word1.equals("edit") || word1.equals("modify") || word1.equals("update")) &&
            (word2.equals("edit") || word2.equals("modify") || word2.equals("update"))) {
            return true;
        }
        return false;
    }

    /**
     * Determines if two sentences are semantically similar using text normalization.
     * 
     * Performs basic similarity checking through:
     * - Case normalization
     * - Punctuation removal
     * - Whitespace normalization
     * - Exact text comparison after normalization
     * 
     * A more sophisticated implementation could use semantic embeddings,
     * Jaccard similarity, or other NLP techniques for better accuracy.
     * 
     * @param sentence1 First sentence to compare
     * @param sentence2 Second sentence to compare
     * @return true if sentences are similar after normalization, false otherwise
     */
    private boolean areSentencesSimilar(String sentence1, String sentence2) {
        // Normalize text by removing punctuation and extra whitespace
        String normalized1 = sentence1.toLowerCase().replaceAll("[^a-z0-9\\s]", "").trim();
        String normalized2 = sentence2.toLowerCase().replaceAll("[^a-z0-9\\s]", "").trim();
        return normalized1.equals(normalized2);
    }

    /**
     * Helper record for tracking story pairs with their redundancy type.
     * 
     * Enables deduplication of problem reporting by maintaining both the story pair
     * and the type of redundancy detected (REDUNDANT_GOAL or REDUNDANT_BENEFIT).
     * 
     * Implements order-agnostic equality to handle (story1, story2) and (story2, story1)
     * as the same pair for deduplication purposes.
     */
    private record StoryPairWithType(UserStory story1, UserStory story2, String type) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            StoryPairWithType that = (StoryPairWithType) o;
            // Order-agnostic comparison: (A,B) equals (B,A) for same type
            return type.equals(that.type) &&
                   ((story1.equals(that.story1) && story2.equals(that.story2)) ||
                    (story1.equals(that.story2) && story2.equals(that.story1)));
        }

        @Override
        public int hashCode() {
            // Order-agnostic hashcode using XOR for story combination
            int storyHash = story1.hashCode() ^ story2.hashCode();
            return 31 * storyHash + type.hashCode();
        }
    }
}
