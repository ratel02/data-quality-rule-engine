package com.cdq.dataquality.ruleengine.rule;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.cdq.dataquality.ruleengine.rule.Rule.COUNTRY_SCOPE_WORLD;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RuleTest {

    private final Rule worldRule = new Rule(null, null, null, null, COUNTRY_SCOPE_WORLD, null, null, null) {
        @Override
        public String evaluate(Map<String, Object> record, Map<String, String> provenance) {
            return "";
        }
    };
    private final Rule deRule = new Rule(null, null, null, null, "DE", null, null, null) {
        @Override
        public String evaluate(Map<String, Object> record, Map<String, String> provenance) {
            return "";
        }
    };

    @Nested
    class TestIsInCountryScope {

        @Test
        void worldIsAlwaysTrue() {
            // given
            Map<String, Object> record = Map.of(
                    "id", "1",
                    "country", "FR"
            );

            // when
            boolean outcome = worldRule.isInCountryScope(record);

            // then
            assertTrue(outcome);
        }

        @Test
        void countryMatch() {
            // given
            Map<String, Object> record = Map.of(
                    "id", "1",
                    "country", "DE"
            );

            // when
            boolean outcome = deRule.isInCountryScope(record);

            // then
            assertTrue(outcome);
        }

        @Test
        void countryMismatch() {
            // given
            Map<String, Object> record = Map.of(
                    "id", "1",
                    "country", "FR"
            );

            // when
            boolean outcome = deRule.isInCountryScope(record);

            // then
            assertFalse(outcome);
        }

        @Test
        void countryNull() {
            // given
            Map<String, Object> record = Map.of(
                    "id", "1"
            );

            // when
            boolean outcome = deRule.isInCountryScope(record);

            // then
            assertFalse(outcome);
        }

    }

}
