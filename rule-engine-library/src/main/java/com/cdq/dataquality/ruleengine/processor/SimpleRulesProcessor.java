package com.cdq.dataquality.ruleengine.processor;

import com.cdq.dataquality.ruleengine.rule.Decision;
import com.cdq.dataquality.ruleengine.rule.Rule;
import com.cdq.dataquality.ruleengine.result.RuleResult;
import com.cdq.dataquality.ruleengine.result.RuleResult.RuleResultBuilder;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Objects.nonNull;

/**
 * A simple rule processor that just processes all rules.
 * See ./design/Design.MD for alternative ideas.
 */
@Setter
public class SimpleRulesProcessor implements RulesProcessor {

    /**
     * Processes all the rules.
     *
     * @param record the schema-less record
     * @param rules  all rules that apply
     * @return rule results
     */
    public List<RuleResult> processRules(final Map<String, Object> record, final List<Rule> rules) {
        // find subset of rules to be executed based on filters, execute each rule and return results
        return rules.stream()
                .map(rule -> this.processRule(record, rule))
                .toList();
    }

    /**
     * Processes one particular rule.
     *
     * @param record record to be processed
     * @param rule   rule to be processed
     * @return rule result
     */
    private RuleResult processRule(final Map<String, Object> record, final Rule rule) {
        // create provenance map to capture execution details and use builder pattern to start building the result
        final Map<String, String> provenance = new HashMap<>();
        final RuleResultBuilder resultBuilder = RuleResult.builder()
                .ruleId(rule.getId())
                .severity(rule.getSeverity())
                .provenance(provenance);

        try {
            // if rule is in country scope, get outcome, otherwise no outcome
            final String outcome = rule.isInCountryScope(record) ? rule.evaluate(record, provenance) : null;

            // map to decision and finish building result
            final Decision decision = nonNull(outcome)
                    ? rule.getDecisionMapping().getOrDefault(outcome, rule.getDefaultDecision())
                    : rule.getDefaultDecision();
            return resultBuilder.success(true)
                    .outcome(outcome)
                    .decision(decision)
                    .build();
        } catch (Exception e) {
            // add error details:
            // - this could be reduced (to avoid exposing details) or
            // - extended to add more details if ok to do so e.g. different error catching and handling
            provenance.put("error", format("Rule execution failed: %s", e.getMessage()));
            return resultBuilder.success(false)
                    .build();
        }
    }

}
