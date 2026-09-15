package com.cdq.dataquality.ruleengine.result;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;


@Getter
@RequiredArgsConstructor
public class RecordResult {

    private final UUID id;
    private final String recordId;
    private final List<RuleResult> ruleResultsSuccess;
    private final List<RuleResult> ruleResultsFailure;

}
