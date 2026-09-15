package com.cdq.dataquality.ruleengine;

import com.cdq.dataquality.ruleengine.catalog.RulesCatalog;
import com.cdq.dataquality.ruleengine.processor.RulesProcessor;
import com.cdq.dataquality.ruleengine.result.BatchSummary;
import com.cdq.dataquality.ruleengine.result.RecordResult;
import com.cdq.dataquality.ruleengine.rule.Decision;
import com.cdq.dataquality.ruleengine.rule.Rule;
import com.cdq.dataquality.ruleengine.result.RuleResult;
import com.cdq.dataquality.ruleengine.rule.Severity;
import com.cdq.dataquality.ruleengine.rule.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.cdq.dataquality.ruleengine.rule.Decision.INVALID;
import static com.cdq.dataquality.ruleengine.rule.Decision.VALID;
import static com.cdq.dataquality.ruleengine.rule.Severity.ERROR;
import static com.cdq.dataquality.ruleengine.rule.Severity.INFO;
import static com.cdq.dataquality.ruleengine.rule.Status.DRAFT;
import static com.cdq.dataquality.ruleengine.rule.Status.RELEASED;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class RuleEngineTest {

    private RulesProcessor rulesProcessor;
    private RulesCatalog rulesCatalog;
    private RuleEngine engine;

    @BeforeEach
    void setUp() {
        rulesProcessor = mock(RulesProcessor.class);
        rulesCatalog = mock(RulesCatalog.class);
        engine = new RuleEngine(rulesProcessor, rulesCatalog);
    }

    /**
     * All tests related to batch summary.
     */
    @Nested
    class ProcessBatchTestSummary {

        @Test
        void countsSuccessfulAndFailedRecordsAndGroupsByDecisionAndSeverity() {
            // given
            Rule rule = mockRule(RELEASED, "DE", List.of("quality"));
            when(rulesCatalog.getRules()).thenReturn(List.of(rule));
            RuleResult success = mockRuleResult(true, VALID, ERROR);
            RuleResult failure = mockRuleResult(false, VALID, ERROR);
            RuleResult invalid = mockRuleResult(true, INVALID, ERROR);
            RuleResult info = mockRuleResult(true, INVALID, INFO);
            when(rulesProcessor.processRules(anyMap(), eq(List.of(rule)))).thenAnswer(invocation -> {
                Map<String, Object> record = invocation.getArgument(0);
                return switch ((String) record.get("id")) {
                    case "ok" -> List.of(success);
                    case "bad" -> List.of(failure);
                    case "invalid" -> List.of(invalid);
                    case "info" -> List.of(info);
                    default -> List.of();
                };
            });
            List<Map<String, Object>> records = List.of(
                    Map.of("id", "ok"),
                    Map.of("id", "bad"),
                    Map.of("id", "invalid"),
                    Map.of("id", "info")
            );

            // when
            BatchSummary summary = engine.processBatch(records, null, null)
                    .getBatchSummary();

            // then
            assertEquals(3, summary.getRuleResultsSuccess());
            assertEquals(1, summary.getRuleResultsFailure());
            assertEquals(1, summary.getRuleResultsByDecision().get(VALID));
            assertEquals(2, summary.getRuleResultsByDecision().get(INVALID));
            assertEquals(2, summary.getRuleResultsBySeverity().get(ERROR));
            assertEquals(1, summary.getRuleResultsBySeverity().get(INFO));
        }

        @Test
        void returnsEmptySummaryForEmptyRecords() {
            // given
            when(rulesCatalog.getRules()).thenReturn(List.of());

            // when
            BatchSummary summary = engine.processBatch(List.of(), null, null)
                    .getBatchSummary();

            // then
            assertEquals(0, summary.getRuleResultsSuccess());
            assertEquals(0, summary.getRuleResultsFailure());
            assertTrue(summary.getRuleResultsByDecision().isEmpty());
            assertTrue(summary.getRuleResultsBySeverity().isEmpty());
            verifyNoInteractions(rulesProcessor);
        }
    }

    /**
     * All tests related to batch record results.
     */
    @Nested
    class ProcessBatchTestRecordResults {

        @Test
        void processesAllRulesWhenNoFiltersAreSupplied() {
            // given
            Rule rule = mockRule(RELEASED, "DE", List.of("quality"));
            RuleResult result = mockRuleResult(true, VALID, ERROR);
            when(rulesCatalog.getRules()).thenReturn(List.of(rule));
            when(rulesProcessor.processRules(anyMap(), eq(List.of(rule)))).thenReturn(List.of(result));
            List<Map<String, Object>> records = List.of(
                    Map.of("id", "1"),
                    Map.of("id", "2")
            );

            // when
            List<RecordResult> results = engine.processBatch(records, null, null)
                    .getRecordResults();

            // then
            assertEquals(2, results.size());
            assertEquals(List.of("1", "2"), extract(results, RecordResult::getRecordId));
            assertEquals(2, extract(results, RecordResult::getId).size());
            for (RecordResult recordResult : results) {
                assertEquals(1, recordResult.getRuleResultsSuccess().size());
                assertEquals(0, recordResult.getRuleResultsFailure().size());
            }
            verify(rulesProcessor, times(2)).processRules(anyMap(), eq(List.of(rule)));
        }

        @Test
        void filtersRulesByStatusAndCategory() {
            // given
            Rule matching = mockRule(RELEASED, "DE", List.of("quality"));
            Rule wrongStatus = mockRule(DRAFT, "DE", List.of("quality"));
            Rule wrongCategory = mockRule(RELEASED, "DE", List.of("other"));
            when(rulesCatalog.getRules()).thenReturn(List.of(matching, wrongStatus, wrongCategory));
            when(rulesProcessor.processRules(anyMap(), eq(List.of(matching)))).thenReturn(List.of());
            List<Map<String, Object>> records = List.of(Map.of(
                    "id", "1",
                    "country", "DE"
            ));

            // when
            List<RecordResult> results = engine.processBatch(records, RELEASED, "quality")
                    .getRecordResults();

            // then
            assertEquals(1, results.size());
            assertTrue(results.getFirst().getRuleResultsSuccess().isEmpty());
            verify(rulesProcessor).processRules(argThat(m -> "1".equals(m.get("id"))), eq(List.of(matching)));
            verify(rulesProcessor, never()).processRules(anyMap(), eq(List.of(wrongStatus)));
            verify(rulesProcessor, never()).processRules(anyMap(), eq(List.of(wrongCategory)));
        }

        @Test
        void excludesRulesWithNullCategoriesWhenCategoryFilterIsSet() {
            // given
            Rule rule = mockRule(Status.values()[0], "DE", null);
            when(rulesCatalog.getRules()).thenReturn(List.of(rule));
            when(rulesProcessor.processRules(anyMap(), eq(emptyList()))).thenReturn(emptyList());
            List<Map<String, Object>> records = List.of(Map.of("id", "1"));

            // when
            List<RecordResult> results = engine.processBatch(records, null, "quality")
                    .getRecordResults();

            // then
            assertEquals(1, results.size());
            assertTrue(results.getFirst().getRuleResultsSuccess().isEmpty());
            verify(rulesProcessor).processRules(anyMap(), eq(emptyList()));
        }

        @Test
        void usesMissingIdMessageWhenRecordIdIsAbsent() {
            // given
            when(rulesCatalog.getRules()).thenReturn(List.of());
            when(rulesProcessor.processRules(anyMap(), eq(List.of()))).thenReturn(List.of());
            List<Map<String, Object>> records = List.of(Map.of());

            // when
            List<RecordResult> results = engine.processBatch(records, null, null)
                    .getRecordResults();

            // then
            assertEquals(1, results.size());
            assertEquals("Record ID not found!", results.getFirst().getRecordId());
            assertTrue(results.getFirst().getRuleResultsSuccess().isEmpty());
            assertTrue(results.getFirst().getRuleResultsFailure().isEmpty());
        }

        @Test
        void convertsNonStringIdToString() {
            // given
            when(rulesCatalog.getRules()).thenReturn(List.of());
            when(rulesProcessor.processRules(anyMap(), eq(List.of()))).thenReturn(List.of());
            List<Map<String, Object>> records = List.of(Map.of("id", 42));

            // when
            List<RecordResult> results = engine.processBatch(records, null, null)
                    .getRecordResults();

            // then
            assertEquals("42", results.getFirst().getRecordId());
        }

        @Test
        void separatesSuccessfulAndFailedRuleResults() {
            // given
            Rule rule = mockRule(RELEASED, "DE", List.of("quality"));
            RuleResult success = mockRuleResult(true, Decision.values()[0], Severity.values()[0]);
            RuleResult failure = mockRuleResult(false, Decision.values()[0], Severity.values()[0]);
            when(rulesCatalog.getRules()).thenReturn(List.of(rule));
            List<Map<String, Object>> records = List.of(
                    Map.of("id", "1"),
                    Map.of("id", "2")
            );
            when(rulesProcessor.processRules(eq(Map.of("id", "1")), eq(List.of(rule)))).thenReturn(List.of(success));
            when(rulesProcessor.processRules(eq(Map.of("id", "2")), eq(List.of(rule)))).thenReturn(List.of(failure));

            // when
            List<RecordResult> results = engine.processBatch(records, null, null)
                    .getRecordResults();

            // then: assert first record was successful, second record was unsuccessful
            assertEquals(1, results.getFirst().getRuleResultsSuccess().size());
            assertEquals(0, results.getFirst().getRuleResultsFailure().size());
            assertEquals(0, results.getLast().getRuleResultsSuccess().size());
            assertEquals(1, results.getLast().getRuleResultsFailure().size());
        }

    }

    private <T> List<T> extract(List<RecordResult> results, Function<RecordResult, T> mapper) {
        return results.stream()
                .map(mapper)
                .toList();
    }

    private static Rule mockRule(Status status, String countryScope, List<String> categories) {
        Rule rule = mock(Rule.class);
        when(rule.getStatus()).thenReturn(status);
        when(rule.getCountryScope()).thenReturn(countryScope);
        when(rule.getCategories()).thenReturn(categories);
        return rule;
    }

    private static RuleResult mockRuleResult(boolean success, Decision decision, Severity severity) {
        return RuleResult.builder()
                .success(success)
                .decision(decision)
                .severity(severity)
                .outcome(success ? "ok" : "bad")
                .provenance(Map.of())
                .ruleId("rule-1")
                .build();
    }

}
