package com.cdq.dataquality.ruleengine.rule.implementations;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static com.cdq.dataquality.ruleengine.rule.Rule.FIELD_NAME_ROOT;
import static org.junit.jupiter.api.Assertions.*;

class IbanFormatRuleTest {

    private final IbanFormatRule rule = new IbanFormatRule(null, null, null, null, null, null, null, null);

    @Nested
    class TestEvaluate {

        @Test
        void nullRecordReturnsBadAndRecordsProvenance() {
            // given
            Map<String, String> provenance = new HashMap<>();

            // when
            String outcome = rule.evaluate(null, provenance);

            // then
            assertEquals("bad", outcome);
            assertEquals("Record is null", provenance.get(FIELD_NAME_ROOT));
        }

        @Test
        void nullIbanReturnsBadAndRecordsMissingIban() {
            // given
            Map<String, String> provenance = new HashMap<>();
            Map<String, Object> record = Map.of("country", "DE");

            // when
            String outcome = rule.evaluate(record, provenance);

            // then
            assertEquals("bad", outcome);
            assertEquals("IBAN is missing, empty or not a string", provenance.get("$.iban"));
        }

        @Test
        void blankIbanReturnsBadAndRecordsMissingIban() {
            // given
            Map<String, String> provenance = new HashMap<>();
            Map<String, Object> record = Map.of("iban", "");

            // when
            String outcome = rule.evaluate(record, provenance);

            // then
            assertEquals("bad", outcome);
            assertEquals("IBAN is missing, empty or not a string", provenance.get("$.iban"));
        }

        @Test
        void nullCountryReturnsBadAndRecordsMissingCountry() {
            // given
            Map<String, String> provenance = new HashMap<>();
            Map<String, Object> record = Map.of("iban", "DE89370400440532013000");

            // when
            String outcome = rule.evaluate(record, provenance);

            // then
            assertEquals("bad", outcome);
            assertEquals("Country is missing, empty or not a string", provenance.get("$.country"));
        }

        @Test
        void blankCountryReturnsBadAndRecordsMissingCountry() {
            // given
            Map<String, String> provenance = new HashMap<>();
            Map<String, Object> record = Map.of(
                    "iban", "DE89370400440532013000",
                    "country", ""
            );

            // when
            String outcome = rule.evaluate(record, provenance);

            // then
            assertEquals("bad", outcome);
            assertEquals("Country is missing, empty or not a string", provenance.get("$.country"));
        }

        @Test
        void ibanStartingWithCountryReturnsOk() {
            // given
            Map<String, String> provenance = new HashMap<>();
            Map<String, Object> record = Map.of(
                    "iban", "DE89370400440532013000",
                    "country", "DE"
            );

            // when
            String outcome = rule.evaluate(record, provenance);

            // then
            assertEquals("ok", outcome);
            assertTrue(provenance.isEmpty());
        }

        @Test
        void ibanNotStartingWithCountryReturnsBadAndRecordsInvalidIban() {
            // given
            Map<String, String> provenance = new HashMap<>();
            Map<String, Object> record = Map.of(
                    "iban", "DE89370400440532013000",
                    "country", "PL"
            );

            // when
            String outcome = rule.evaluate(record, provenance);

            // then
            assertEquals("bad", outcome);
            assertEquals("Invalid IBAN - doesn't start with country", provenance.get("$.iban"));
        }

        @Test
        void nonStringIbanAndCountry() {
            // given
            Map<String, String> provenance = new HashMap<>();
            Map<String, Object> record = Map.of(
                    "iban", 123,
                    "country", 567
            );

            // when
            String outcome = rule.evaluate(record, provenance);

            // then
            assertEquals("bad", outcome);
        }

    }

}