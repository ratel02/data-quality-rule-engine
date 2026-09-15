package com.cdq.dataquality.ruleengine.result;

import com.cdq.dataquality.ruleengine.rule.Decision;
import com.cdq.dataquality.ruleengine.rule.Severity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Getter
@RequiredArgsConstructor
public class BatchSummary {

    private final int ruleResultsSuccess;
    private final int ruleResultsFailure;
    private final Map<Decision, Long> ruleResultsByDecision;
    private final Map<Severity, Long> ruleResultsBySeverity;

}
