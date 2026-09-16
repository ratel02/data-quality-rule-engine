package com.cdq.dataquality.ruleengine.processor;

import com.cdq.dataquality.ruleengine.rule.Rule;
import com.cdq.dataquality.ruleengine.result.RuleResult;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

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

    /**
     * Executes all rules against one single record in a stream way.
     * Results are not accumulated, instead each rule result is emitted as soon as rule is processed.
     *
     * @param record        the schema-less record
     * @param rules         all rules that apply
     * @param resultHandler a consumer that handles rule processed action
     */
    void processRulesStream(Map<String, Object> record, List<Rule> rules, Consumer<RuleResult> resultHandler);

}
