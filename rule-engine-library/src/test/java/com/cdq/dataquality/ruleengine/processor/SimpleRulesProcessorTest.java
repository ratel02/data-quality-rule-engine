package com.cdq.dataquality.ruleengine.processor;

import com.cdq.dataquality.ruleengine.rule.Decision;
import com.cdq.dataquality.ruleengine.rule.Rule;
import com.cdq.dataquality.ruleengine.result.RuleResult;
import com.cdq.dataquality.ruleengine.rule.Severity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.cdq.dataquality.ruleengine.rule.Decision.INVALID;
import static com.cdq.dataquality.ruleengine.rule.Decision.NOT_APPLICABLE;
import static com.cdq.dataquality.ruleengine.rule.Decision.VALID;
import static com.cdq.dataquality.ruleengine.rule.Severity.ERROR;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SimpleRulesProcessorTest {

    private SimpleRulesProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new SimpleRulesProcessor();
    }

    @Nested
    class TestProcessRules {
        @Test
        void returnsOneSuccessfulResultPerRule() {
            // given
            Rule rule1 = mockRule("r1", ERROR, "ok", VALID);
            Rule rule2 = mockRule("r2", ERROR, "bad", VALID);

            // when
            List<RuleResult> results = processor.processRules(Map.of("id", "1"), List.of(rule1, rule2));

            // then
            assertEquals(2, results.size());
            assertEquals(List.of(true, true), extract(results, RuleResult::isSuccess));
            assertEquals(List.of("ok", "bad"), extract(results, RuleResult::getOutcome));
            assertEquals(List.of(VALID, VALID), extract(results, RuleResult::getDecision));
            assertEquals(List.of(ERROR, ERROR), extract(results, RuleResult::getSeverity));
            assertEquals(List.of("r1", "r2"), extract(results, RuleResult::getRuleId));
            assertEquals(List.of("1", "1"), extract(results, RuleResult::getRecordId));
        }

        @Test
        void usesMappedDecisionWhenOutcomeExistsInMapping() {
            // given
            Rule rule = mock(Rule.class);
            when(rule.getId()).thenReturn("r1");
            when(rule.getSeverity()).thenReturn(ERROR);
            when(rule.getDecisionMapping()).thenReturn(Map.of("blocked", INVALID));
            when(rule.getDefaultDecision()).thenReturn(NOT_APPLICABLE);
            when(rule.isInCountryScope(anyMap())).thenReturn(true);
            when(rule.evaluate(any(), any())).thenReturn("blocked");

            // when
            RuleResult result = processor.processRules(Map.of("id", "1"), List.of(rule)).getFirst();

            // then
            assertTrue(result.isSuccess());
            assertEquals("blocked", result.getOutcome());
            assertEquals(INVALID, result.getDecision());
            assertEquals("1", result.getRecordId());
        }

        @Test
        void usesDefaultDecisionWhenOutcomeIsNotMapped() {
            // given
            Rule rule = mock(Rule.class);
            when(rule.getId()).thenReturn("r1");
            when(rule.isInCountryScope(anyMap())).thenReturn(true);
            when(rule.getSeverity()).thenReturn(ERROR);
            when(rule.getDecisionMapping()).thenReturn(Map.of("blocked", INVALID));
            when(rule.getDefaultDecision()).thenReturn(NOT_APPLICABLE);
            when(rule.evaluate(any(), any())).thenReturn("unknown-outcome");

            // when
            RuleResult result = processor.processRules(Map.of("id", "1"), List.of(rule)).getFirst();

            // then
            assertTrue(result.isSuccess());
            assertEquals("unknown-outcome", result.getOutcome());
            assertEquals(NOT_APPLICABLE, result.getDecision());
            assertEquals("1", result.getRecordId());
        }

        @Test
        void usesDefaultDecisionWhenRuleIsNotInScope() {
            // given
            Rule rule = mock(Rule.class);
            when(rule.getId()).thenReturn("r1");
            when(rule.getSeverity()).thenReturn(ERROR);
            when(rule.isInCountryScope(anyMap())).thenReturn(false);
            when(rule.getDecisionMapping()).thenReturn(Map.of("blocked", INVALID));
            when(rule.getDefaultDecision()).thenReturn(NOT_APPLICABLE);

            // when
            RuleResult result = processor.processRules(Map.of("id", "1"), List.of(rule)).getFirst();

            // then
            assertTrue(result.isSuccess());
            assertNull(null, result.getOutcome());
            assertEquals(NOT_APPLICABLE, result.getDecision());
            assertEquals("1", result.getRecordId());
        }

        @Test
        void marksRuleAsFailedWithGenericProvenanceWhenEvaluationFails() {
            // given
            Severity severity = ERROR;
            Rule rule = mock(Rule.class);
            when(rule.getId()).thenReturn("r1");
            when(rule.getSeverity()).thenReturn(severity);
            when(rule.isInCountryScope(anyMap())).thenReturn(true);
            when(rule.evaluate(any(), any())).thenThrow(new IllegalStateException("error"));

            // when
            RuleResult result = processor.processRules(Map.of("id", "1"), List.of(rule)).getFirst();

            // then
            assertFalse(result.isSuccess());
            assertEquals("r1", result.getRuleId());
            assertEquals("1", result.getRecordId());
            assertEquals(severity, result.getSeverity());
            assertNull(result.getOutcome());
            assertNull(result.getDecision());
            assertEquals("Rule execution failed: error", result.getProvenance().get("error"));
        }

        @Test
        void returnsEmptyListWhenThereAreNoRules() {
            assertTrue(processor.processRules(Map.of("id", "1"), List.of()).isEmpty());
        }

    }

    @Nested
    class TestProcessRulesStream {

        @Test
        void emitsOneSuccessfulResultPerRule() {
            // given
            Rule rule1 = mockRule("r1", ERROR, "ok", VALID);
            Rule rule2 = mockRule("r2", ERROR, "bad", VALID);
            final List<RuleResult> results = new ArrayList<>();

            // when
            processor.processRulesStream(Map.of("id", "1"), List.of(rule1, rule2), results::add);

            // then
            assertEquals(2, results.size());
            assertEquals(List.of(true, true), extract(results, RuleResult::isSuccess));
            assertEquals(List.of("ok", "bad"), extract(results, RuleResult::getOutcome));
            assertEquals(List.of(VALID, VALID), extract(results, RuleResult::getDecision));
            assertEquals(List.of(ERROR, ERROR), extract(results, RuleResult::getSeverity));
            assertEquals(List.of("r1", "r2"), extract(results, RuleResult::getRuleId));
            assertEquals(List.of("1", "1"), extract(results, RuleResult::getRecordId));
        }

        @Test
        void notingIsEmittedWhenThereAreNoRules() {
            // given
            final List<RuleResult> results = new ArrayList<>();

            // when
            processor.processRulesStream(Map.of("id", "1"), List.of(), results::add);

            // then
            assertTrue(results.isEmpty());
        }

    }

    private static Rule mockRule(String id, Severity severity, String outcome, Decision decision) {
        Rule rule = mock(Rule.class);
        when(rule.getId()).thenReturn(id);
        when(rule.getSeverity()).thenReturn(severity);
        when(rule.getDecisionMapping()).thenReturn(Map.of(outcome, decision));
        when(rule.getDefaultDecision()).thenReturn(decision);
        when(rule.evaluate(any(), any())).thenReturn(outcome);
        when(rule.isInCountryScope(anyMap())).thenReturn(true);
        return rule;
    }

    private static <T> List<T> extract(List<RuleResult> results, Function<RuleResult, T> mapper) {
        return results.stream().map(mapper).toList();
    }

}
