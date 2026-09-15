package com.cdq.dataquality.ruleengine.rule.implementations;

import com.cdq.dataquality.ruleengine.rule.Decision;
import com.cdq.dataquality.ruleengine.rule.Rule;
import com.cdq.dataquality.ruleengine.rule.Severity;
import com.cdq.dataquality.ruleengine.rule.Status;

import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Objects.isNull;

public class VatFormatRule extends Rule {

    private static final String FIELD_NAME_VAT_ID = "vatId";
    private static final int MIN_LENGTH = 9;
    private static final String OUTCOME_OK = "ok";
    private static final String OUTCOME_BAD = "bad";

    /**
     * Creates new vat format rule.
     */
    public VatFormatRule(String id,
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
     * Validates the record's vat.
     *
     * @param record     a record to be evaluated
     * @param provenance a map to provide rule execution details
     * @return string outcome
     */
    @Override
    public String evaluate(final Map<String, Object> record,
                           final Map<String, String> provenance) {
        // ensure not null
        if (isNull(record)) {
            provenance.put(FIELD_NAME_ROOT, "Record is null");
            return OUTCOME_BAD;
        }

        // get vatId and verify it
        final Object vatIdField = record.get(FIELD_NAME_VAT_ID);
        if (!(vatIdField instanceof String vatId) || vatId.isBlank()) {
            provenance.put(FIELD_NAME_ROOT + "." + FIELD_NAME_VAT_ID, "Vat ID is missing, empty or not a string");
            return OUTCOME_BAD;
        }

        // check country (case-sensitive)
        if (vatId.length() >= MIN_LENGTH) {
            return OUTCOME_OK;
        } else {
            provenance.put(FIELD_NAME_ROOT + "." + FIELD_NAME_VAT_ID, format("Invalid length: %d", vatId.length()));
            return OUTCOME_BAD;
        }
    }

}
