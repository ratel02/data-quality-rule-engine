package com.cdq.dataquality.ruleengine.rule;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

import static java.util.Objects.nonNull;

@Getter
@RequiredArgsConstructor
public abstract class Rule {

    public static final String FIELD_NAME_ROOT = "$";
    public static final String FIELD_NAME_COUNTRY = "country";
    public static final String COUNTRY_SCOPE_WORLD = "WORLD";

    private final String id;
    private final String label;
    private final Status status;
    private final Severity severity;
    private final String countryScope;
    private final List<String> categories;
    private final Map<String, Decision> decisionMapping;
    private final Decision defaultDecision;

    /**
     * Each rule implementation has to provide evaluate method.
     * Please see /design/Design.md for alternative ideas.
     *
     * @param record     a record to be evaluated
     * @param provenance a map to provide rule execution details
     * @return outcome of the execution
     */
    public abstract String evaluate(final Map<String, Object> record,
                                    final Map<String, String> provenance);

    /**
     * Verifies whether a rule is in scope or not.
     * It's a per-record filter.
     *
     * @param record record to be verified
     * @return true if rule is WORLD scoped or has non-null country that matches record country; false otherwise
     */
    public boolean isInCountryScope(final Map<String, Object> record) {
        return COUNTRY_SCOPE_WORLD.equals(this.countryScope)
                || (nonNull(this.countryScope) && this.countryScope.equals(record.get(FIELD_NAME_COUNTRY)));
    }

}
