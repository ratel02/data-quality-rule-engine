package com.cdq.dataquality.ruleengine.rule.implementations;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CountryBlockedRuleTest {

    private final CountryBlockedRule rule = new CountryBlockedRule(null, null, null, null, null, null, null, null);

    @Nested
    class TestEvaluate {

        @Test
        void nullRecordReturnsOkAndRecordsProvenance() {
            // given
            Map<String, String> provenance = new HashMap<>();

            // when
            String outcome = rule.evaluate(null, provenance);

            // then
            assertEquals("ok", outcome);
            assertEquals("Record is null", provenance.get("$"));
        }

        @Test
        void nullCountryReturnsOkAndRecordsMissingCountry() {
            // given
            Map<String, String> provenance = new HashMap<>();
            Map<String, Object> record = Map.of("name", "A");

            // when
            String outcome = rule.evaluate(record, provenance);

            // then
            assertEquals("ok", outcome);
            assertEquals("Country is missing, empty or not a string", provenance.get("$.country"));
        }

        @Test
        void blankCountryReturnsOkAndRecordsMissingCountry() {
            // given
            Map<String, String> provenance = new HashMap<>();
            Map<String, Object> record = Map.of("country", "");

            // when
            String outcome = rule.evaluate(record, provenance);

            // then
            assertEquals("ok", outcome);
            assertEquals("Country is missing, empty or not a string", provenance.get("$.country"));
        }

        @Test
        void whitespaceOnlyReturnsOkAndRecordsMissingCountry() {
            // given
            Map<String, String> provenance = new HashMap<>();
            Map<String, Object> record = Map.of("country", "   ");

            // when
            String outcome = rule.evaluate(record, provenance);

            // then
            assertEquals("ok", outcome);
            assertEquals("Country is missing, empty or not a string", provenance.get("$.country"));
        }

        @Test
        void differentCountryIsNotBeingBlocked() {
            // given
            Map<String, String> provenance = new HashMap<>();
            Map<String, Object> record = Map.of("country", "FR");

            // when
            String outcome = rule.evaluate(record, provenance);

            // then
            assertEquals("ok", outcome);
            assertTrue(provenance.isEmpty());
        }

        @Test
        void countryZZIsBeingBlocked() {
            // given
            Map<String, String> provenance = new HashMap<>();
            Map<String, Object> record = Map.of("country", "ZZ");

            // when
            String outcome = rule.evaluate(record, provenance);

            // then
            assertEquals("blocked", outcome);
            assertEquals("Country is blocked", provenance.get("$.country"));
        }

        @Test
        void nonStringCountryIsConvertedButDoesNotMatch() {
            // given
            Map<String, String> provenance = new HashMap<>();
            Map<String, Object> record = Map.of("country", 123);

            // when
            String outcome = rule.evaluate(record, provenance);

            // then
            assertEquals("ok", outcome);
        }

    }

}
