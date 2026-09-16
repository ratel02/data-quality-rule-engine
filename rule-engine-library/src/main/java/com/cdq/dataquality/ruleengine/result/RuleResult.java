package com.cdq.dataquality.ruleengine.result;

import com.cdq.dataquality.ruleengine.rule.Decision;
import com.cdq.dataquality.ruleengine.rule.Severity;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Getter
@Builder
@RequiredArgsConstructor
public class RuleResult {

    private final String recordId;
    private final String ruleId;
    private final String outcome;
    private final Decision decision;
    private final Severity severity;
    private final boolean success;
    private final Map<String, String> provenance;

}
