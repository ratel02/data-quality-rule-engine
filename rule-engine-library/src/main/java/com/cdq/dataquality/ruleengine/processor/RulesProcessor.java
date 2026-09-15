package com.cdq.dataquality.ruleengine.processor;

import com.cdq.dataquality.ruleengine.rule.Rule;
import com.cdq.dataquality.ruleengine.result.RuleResult;

import java.util.List;
import java.util.Map;

/**
 * An interface for processing the rules.
 */
public interface RulesProcessor {

    /**
     * Executes all rules against one single record.
     *
     * @param record the schema-less record
     * @param rules  all rules that apply
     * @return list of rule results
     */
    List<RuleResult> processRules(Map<String, Object> record, List<Rule> rules);

}
