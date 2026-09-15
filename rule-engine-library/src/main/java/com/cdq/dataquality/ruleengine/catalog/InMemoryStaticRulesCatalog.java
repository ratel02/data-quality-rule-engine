package com.cdq.dataquality.ruleengine.catalog;

import com.cdq.dataquality.ruleengine.rule.Rule;
import com.cdq.dataquality.ruleengine.rule.implementations.CountryBlockedRule;
import com.cdq.dataquality.ruleengine.rule.implementations.IbanFormatRule;
import com.cdq.dataquality.ruleengine.rule.implementations.VatFormatRule;

import java.util.List;
import java.util.Map;

import static com.cdq.dataquality.ruleengine.rule.Decision.INVALID;
import static com.cdq.dataquality.ruleengine.rule.Decision.NOT_APPLICABLE;
import static com.cdq.dataquality.ruleengine.rule.Decision.VALID;
import static com.cdq.dataquality.ruleengine.rule.Rule.COUNTRY_SCOPE_WORLD;
import static com.cdq.dataquality.ruleengine.rule.Severity.ERROR;
import static com.cdq.dataquality.ruleengine.rule.Severity.WARNING;
import static com.cdq.dataquality.ruleengine.rule.Status.RELEASED;

/**
 * A static in-memory rules-catalog provider.
 */
public class InMemoryStaticRulesCatalog implements RulesCatalog {

    private static final List<Rule> RULES = List.of(
            new CountryBlockedRule(
                    "rule1",
                    "countryBlocked",
                    RELEASED,
                    ERROR,
                    COUNTRY_SCOPE_WORLD,
                    List.of("sanctions"),
                    Map.of("blocked", INVALID),
                    NOT_APPLICABLE
            ),
            new VatFormatRule(
                    "rule2",
                    "vatFormat",
                    RELEASED,
                    WARNING,
                    COUNTRY_SCOPE_WORLD,
                    null,
                    Map.of(
                            "ok", VALID,
                            "bad", INVALID
                    ),
                    NOT_APPLICABLE
            ),
            new IbanFormatRule(
                    "rule3",
                    "ibanFormat",
                    RELEASED,
                    WARNING,
                    COUNTRY_SCOPE_WORLD,
                    null,
                    Map.of(
                            "ok", VALID,
                            "bad", INVALID
                    ),
                    NOT_APPLICABLE
            )
    );

    /**
     * Returns static constant list of rules.
     */
    @Override
    public List<Rule> getRules() {
        return RULES;
    }

}
