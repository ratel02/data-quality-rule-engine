package com.cdq.dataquality.ruleengine.rule.implementations;

import com.cdq.dataquality.ruleengine.rule.Decision;
import com.cdq.dataquality.ruleengine.rule.Rule;
import com.cdq.dataquality.ruleengine.rule.Severity;
import com.cdq.dataquality.ruleengine.rule.Status;

import java.util.List;
import java.util.Map;

import static java.util.Objects.isNull;


public class CountryBlockedRule extends Rule {

    private static final String BLOCKED_COUNTRY = "ZZ";
    private static final String OUTCOME_OK = "ok";
    private static final String OUTCOME_BLOCKED = "blocked";

    /**
     * Creates new country blocked rule.
     */
    public CountryBlockedRule(String id,
                              String label,
                              Status status,
                              Severity severity,
                              String countryScope,
                              List<String> categories,
                              Map<String, Decision> decisionMapping,
                              Decision defaultDecision) {
        super(id, label, status, severity, countryScope, categories, decisionMapping, defaultDecision);
    }

    /**
     * Evaluates country blocked rule.
     *
     * @param record     a record to be evaluated
     * @param provenance a map to provide rule execution details
     * @return outcome
     */
    @Override
    public String evaluate(final Map<String, Object> record,
                           final Map<String, String> provenance) {
        // ensure not null
        if (isNull(record)) {
            provenance.put(FIELD_NAME_ROOT, "Record is null");
            return OUTCOME_OK;
        }

        // get country and verify it
        final Object countryField = record.get(FIELD_NAME_COUNTRY);
        if (!(countryField instanceof String country) || country.isBlank()) {
            provenance.put(FIELD_NAME_ROOT + "." + FIELD_NAME_COUNTRY, "Country is missing, empty or not a string");
            return OUTCOME_OK;
        }

        // check country (case-sensitive)
        if (country.equals(BLOCKED_COUNTRY)) {
            provenance.put(FIELD_NAME_ROOT + "." + FIELD_NAME_COUNTRY, "Country is blocked");
            return OUTCOME_BLOCKED;
        } else {
            return OUTCOME_OK;
        }
    }

}
