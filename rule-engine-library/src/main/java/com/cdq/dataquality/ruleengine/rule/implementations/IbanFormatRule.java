package com.cdq.dataquality.ruleengine.rule.implementations;

import com.cdq.dataquality.ruleengine.rule.Decision;
import com.cdq.dataquality.ruleengine.rule.Rule;
import com.cdq.dataquality.ruleengine.rule.Severity;
import com.cdq.dataquality.ruleengine.rule.Status;

import java.util.List;
import java.util.Map;

import static java.util.Objects.isNull;

public class IbanFormatRule extends Rule {

    private static final String FIELD_NAME_IBAN = "iban";
    private static final String FIELD_NAME_COUNTRY = "country";
    private static final String OUTCOME_OK = "ok";
    private static final String OUTCOME_BAD = "bad";

    /**
     * Creates new iban format rule.
     */
    public IbanFormatRule(String id,
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
     * Validates if iban is valid or not.
     *
     * @param record     a record to be evaluated
     * @param provenance a map to provide rule execution details
     * @return outcome string
     */
    @Override
    public String evaluate(final Map<String, Object> record,
                           final Map<String, String> provenance) {
        // ensure not null
        if (isNull(record)) {
            provenance.put(FIELD_NAME_ROOT, "Record is null");
            return OUTCOME_BAD;
        }

        // get iban and verify it
        final Object ibanField = record.get(FIELD_NAME_IBAN);
        if (!(ibanField instanceof String iban) || iban.isBlank()) {
            provenance.put(FIELD_NAME_ROOT + "." + FIELD_NAME_IBAN, "IBAN is missing, empty or not a string");
            return OUTCOME_BAD;
        }

        // get country and verify it
        final Object countryField = record.get(FIELD_NAME_COUNTRY);
        if (!(countryField instanceof String country) || country.isBlank()) {
            provenance.put(FIELD_NAME_ROOT + "." + FIELD_NAME_COUNTRY, "Country is missing, empty or not a string");
            return OUTCOME_BAD;
        }

        // check iban starts with
        if (iban.startsWith(country)) {
            return OUTCOME_OK;
        } else {
            provenance.put(FIELD_NAME_ROOT + "." + FIELD_NAME_IBAN, "Invalid IBAN - doesn't start with country");
            return OUTCOME_BAD;
        }
    }

}
