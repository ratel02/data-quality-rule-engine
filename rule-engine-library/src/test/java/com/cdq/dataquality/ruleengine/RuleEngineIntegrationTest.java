package com.cdq.dataquality.ruleengine;

import com.cdq.dataquality.ruleengine.catalog.InMemoryStaticRulesCatalog;
import com.cdq.dataquality.ruleengine.processor.SimpleRulesProcessor;
import com.cdq.dataquality.ruleengine.result.BatchResult;
import com.cdq.dataquality.ruleengine.result.RecordResult;
import com.cdq.dataquality.ruleengine.rule.Rule;
import com.cdq.dataquality.ruleengine.result.RuleResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.cdq.dataquality.ruleengine.rule.Decision.INVALID;
import static com.cdq.dataquality.ruleengine.rule.Decision.NOT_APPLICABLE;
import static com.cdq.dataquality.ruleengine.rule.Decision.VALID;
import static com.cdq.dataquality.ruleengine.rule.Rule.COUNTRY_SCOPE_WORLD;
import static com.cdq.dataquality.ruleengine.rule.Rule.FIELD_NAME_COUNTRY;
import static com.cdq.dataquality.ruleengine.rule.Severity.ERROR;
import static com.cdq.dataquality.ruleengine.rule.Severity.WARNING;
import static com.cdq.dataquality.ruleengine.rule.Status.RELEASED;
import static java.util.stream.Collectors.toMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuleEngineIntegrationTest {


    @Test
    void processesProvidedFixtureEndToEndAndBuildsExpectedSummary() {
        // given
        RuleEngine engine = new RuleEngine(new SimpleRulesProcessor(), new InMemoryStaticRulesCatalog());
        List<Map<String, Object>> records = List.of(
                Map.of(
                        "id", "r1",
                        "vatId", "DE111111111",
                        "country", "DE",
                        "legalName", "ACME GmbH",
                        "iban", "DE123456789"
                ),
                Map.of(
                        "id", "r2",
                        "vatId", "FR22",
                        "country", "FR",
                        "legalName", "Bricolage SARL",
                        "iban", "FR123456789"
                ),
                Map.of(
                        "id", "r3",
                        "country", "ZZ",
                        "legalName", "Nowhere Ltd",
                        "iban", ""
                )
        );

        // when
        BatchResult batchResult = engine.processBatch(records, null, null);

        // then: verify record results
        assertEquals(3, batchResult.getRecordResults().size());

        // then: verify all rule results
        final int allRuleResults = batchResult.getRecordResults()
                .stream()
                .mapToInt(result -> result.getRuleResultsSuccess().size() + result.getRuleResultsFailure().size())
                .sum();
        assertEquals(9, allRuleResults);

        // then: verify failure rule results
        final int failureRuleResults = batchResult.getRecordResults().stream()
                .mapToInt(recordResult -> recordResult.getRuleResultsFailure().size())
                .sum();
        assertEquals(0, failureRuleResults);

        // group by records and by rule
        final Map<String, Map<String, RuleResult>> byRecordAndRule = batchResult.getRecordResults()
                .stream()
                .collect(toMap(
                        RecordResult::getRecordId,
                        recordResult -> recordResult.getRuleResultsSuccess()
                                .stream()
                                .collect(toMap(RuleResult::getRuleId, Function.identity()))
                ));

        // then: verify r1
        assertEquals(NOT_APPLICABLE, byRecordAndRule.get("r1").get("rule1").getDecision());
        assertEquals(VALID, byRecordAndRule.get("r1").get("rule2").getDecision());
        assertEquals(VALID, byRecordAndRule.get("r1").get("rule3").getDecision());

        // then: verify r2
        assertEquals(NOT_APPLICABLE, byRecordAndRule.get("r2").get("rule1").getDecision());
        assertEquals(INVALID, byRecordAndRule.get("r2").get("rule2").getDecision());
        assertEquals(VALID, byRecordAndRule.get("r2").get("rule3").getDecision());

        // then: verify r3
        assertEquals(INVALID, byRecordAndRule.get("r3").get("rule1").getDecision());
        assertEquals(INVALID, byRecordAndRule.get("r3").get("rule2").getDecision());
        assertEquals(INVALID, byRecordAndRule.get("r3").get("rule3").getDecision());

        // then: verify counters
        assertEquals(3, batchResult.getBatchSummary().getRuleResultsByDecision().get(VALID));
        assertEquals(4, batchResult.getBatchSummary().getRuleResultsByDecision().get(INVALID));
        assertEquals(2, batchResult.getBatchSummary().getRuleResultsByDecision().get(NOT_APPLICABLE));
        assertEquals(3, batchResult.getBatchSummary().getRuleResultsBySeverity().get(ERROR));
        assertEquals(6, batchResult.getBatchSummary().getRuleResultsBySeverity().get(WARNING));
    }

    @Test
    void isolatesRuleFailureAndContinuesWithOtherRulesAndLaterRecords() {
        // given
        Rule failingRule = new Rule("failingRule", "failingRule", RELEASED, ERROR, COUNTRY_SCOPE_WORLD, null, Map.of("ok", VALID), NOT_APPLICABLE) {
            @Override
            public String evaluate(Map<String, Object> record, Map<String, String> provenance) {
                if ("r1".equals(record.get("id"))) {
                    throw new IllegalStateException("error");
                }
                provenance.put("executed", "true");
                return "ok";
            }
        };
        Rule healthyRule = new Rule("healthyRule", "healthyRule", RELEASED, WARNING, COUNTRY_SCOPE_WORLD, null, Map.of("ok", VALID), NOT_APPLICABLE) {
            @Override
            public String evaluate(Map<String, Object> record, Map<String, String> provenance) {
                provenance.put("executed", "true");
                return "ok";
            }
        };
        RuleEngine engine = new RuleEngine(new SimpleRulesProcessor(), () -> List.of(failingRule, healthyRule));
        List<Map<String, Object>> records = List.of(
                Map.of("id", "r1", FIELD_NAME_COUNTRY, "DE"),
                Map.of("id", "r2", FIELD_NAME_COUNTRY, "DE")
        );

        // when
        BatchResult result = engine.processBatch(records, null, null);

        // then: verify r1 contains one isolated error
        RecordResult first = result.getRecordResults().getFirst();
        assertEquals(1, first.getRuleResultsFailure().size());
        RuleResult firstRecordFailure = first.getRuleResultsFailure().getFirst();
        assertEquals("failingRule", firstRecordFailure.getRuleId());
        assertEquals("Rule execution failed: error", firstRecordFailure.getProvenance().get("error"));
        // then: verify r1 contains rule that was still executed
        assertEquals(1, first.getRuleResultsSuccess().size());
        RuleResult firstRecordSuccess = first.getRuleResultsSuccess().getFirst();
        assertEquals("healthyRule", firstRecordSuccess.getRuleId());

        // then: verify failure on r1 did not terminate the batch - r2 was processed all good
        RecordResult second = result.getRecordResults().getLast();
        assertTrue(second.getRuleResultsFailure().isEmpty());
        assertEquals(2, second.getRuleResultsSuccess().size());
    }

}