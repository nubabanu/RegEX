package de.uni_marburg.sp25.quality;

import de.uni_marburg.sp25.UserStory;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Abstract base class for quality criteria analysis in the INVEST quality framework.
 * 
 * QualityCriterion defines the contract for implementing specific quality analysis algorithms
 * that evaluate user stories against established quality principles. The framework supports:
 * 
 * INVEST Quality Criteria:
 * - Independence: Stories should be self-contained without dependencies
 * - Negotiable: Stories should be flexible and open to discussion
 * - Valuable: Stories should deliver clear business value
 * - Estimable: Stories should be sized appropriately for estimation
 * - Small: Stories should be implementable within a single iteration
 * - Testable: Stories should have clear acceptance criteria
 * 
 * Implementation Architecture:
 * - Template Method Pattern: Provides consistent analysis interface across criteria
 * - Internationalization: Uses ResourceBundle for localized error messages
 * - Extensibility: New quality criteria can be added by extending this class
 * - Integration: Works with QualityAnalysisManager for coordinated analysis
 * 
 * Analysis Process:
 * 1. Receive list of UserStory objects for evaluation
 * 2. Apply criterion-specific algorithms and heuristics
 * 3. Generate QualityProblem objects for identified issues
 * 4. Return comprehensive problem list with localized descriptions
 * 
 * Natural Language Processing:
 * Concrete implementations utilize NLP techniques including pattern matching,
 * entity extraction, dependency analysis, and semantic similarity checking
 * to automatically detect quality issues in user story text.
 * 
 * @see QualityAnalysisManager
 * @see QualityProblem
 * @see UserStory
 */
public abstract class QualityCriterion {
    /** ResourceBundle for internationalized error messages and criterion names */
    protected ResourceBundle messages;
    
    /** Human-readable name of this quality criterion for UI display */
    protected String criterionName;

    /**
     * Constructs a quality criterion with localization support.
     * 
     * @param criterionName The display name of this criterion
     * @param messages ResourceBundle for localized text (English/German support)
     */
    public QualityCriterion(String criterionName, ResourceBundle messages) {
        this.criterionName = criterionName;
        this.messages = messages;
    }

    /**
     * Analyzes the given user stories for this quality criterion
     * @param userStories the list of user stories to analyze
     * @return list of quality problems found
     */
    public abstract List<QualityProblem> analyze(List<UserStory> userStories);

    public String getCriterionName() {
        return criterionName;
    }
}
