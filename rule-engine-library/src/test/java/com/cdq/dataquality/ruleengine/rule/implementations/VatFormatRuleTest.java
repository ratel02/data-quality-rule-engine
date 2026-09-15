package com.cdq.dataquality.ruleengine.rule.implementations;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static com.cdq.dataquality.ruleengine.rule.Rule.FIELD_NAME_ROOT;
import static org.junit.jupiter.api.Assertions.*;

class VatFormatRuleTest {

    private final VatFormatRule rule = new VatFormatRule(null, null, null, null, null, null, null, null);

    @Nested
    class TestEvaluate {

        @Test
        void nullRecordReturnsOkAndRecordsProvenance() {
            // given
            Map<String, String> provenance = new HashMap<>();

            // when
            String outcome = rule.evaluate(null, provenance);

            // then
            assertEquals("bad", outcome);
            assertEquals("Record is null", provenance.get(FIELD_NAME_ROOT));
        }

        @Test
        void nullVatIdReturnsBadAndRecordsMissingVatId() {
            // given
            Map<String, String> provenance = new HashMap<>();
            Map<String, Object> record = Map.of();

            // when
            String outcome = rule.evaluate(record, provenance);

            // then
            assertEquals("bad", outcome);
            assertEquals("Vat ID is missing, empty or not a string", provenance.get("$.vatId"));
        }

        @Test
        void blankVatIdReturnsBadAndRecordsMissingVatId() {
            // given
            Map<String, String> provenance = new HashMap<>();
            Map<String, Object> record = Map.of("vatId", "");

            // when
            String outcome = rule.evaluate(record, provenance);

            // then
            assertEquals("bad", outcome);
            assertEquals("Vat ID is missing, empty or not a string", provenance.get("$.vatId"));
        }

        @Test
        void VatIdWithMinimumLengthReturnsOk() {
            // given
            Map<String, String> provenance = new HashMap<>();
            Map<String, Object> record = Map.of("vatId", "123456789");

            // when
            String outcome = rule.evaluate(record, provenance);

            // then
            assertEquals("ok", outcome);
            assertTrue(provenance.isEmpty());
        }

        @Test
        void vatIdLongerThanMinimumLengthReturnsOk() {
            // given
            Map<String, String> provenance = new HashMap<>();
            Map<String, Object> record = Map.of("vatId", "1234567890");

            // when
            String outcome = rule.evaluate(record, provenance);

            // then
            assertEquals("ok", outcome);
            assertTrue(provenance.isEmpty());
        }

        @Test
        void vatIdShorterThanMinimumLengthReturnsBadAndRecordsInvalidLength() {
            // given
            Map<String, String> provenance = new HashMap<>();
            Map<String, Object> record = Map.of("vatId", "12345678");

            // when
            String outcome = rule.evaluate(record, provenance);

            // then
            assertEquals("bad", outcome);
            assertEquals("Invalid length: 8", provenance.get("$.vatId"));
        }

        @Test
        void nonStringVatIdIsConvertedBeforeValidation() {
            // given
            Map<String, String> provenance = new HashMap<>();
            Map<String, Object> record = Map.of("vatId", 123456789);

            // when
            String outcome = rule.evaluate(record, provenance);

            // then
            assertEquals("bad", outcome);
        }

        @Test
        void whitespaceOnlyVatIdReturnsBadAndRecordsMissingVatId() {
            // given
            Map<String, String> provenance = new HashMap<>();
            Map<String, Object> record = Map.of("vatId", "         ");

            // when
            String outcome = rule.evaluate(record, provenance);

            // then
            assertEquals("bad", outcome);
            assertEquals("Vat ID is missing, empty or not a string", provenance.get("$.vatId"));
        }

    }

}