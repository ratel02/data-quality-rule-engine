package com.cdq.dataquality.ruleengine.catalog;

import com.cdq.dataquality.ruleengine.rule.Rule;

import java.util.List;

/**
 * An interface to load the rules for rule-engine.
 */
public interface RulesCatalog {

    /**
     * Should provide a list of all available rules. Filtering is out of scope.
     *
     * @return all available rules
     */
    List<Rule> getRules();

}
