package com.cdq.dataquality.ruleengine.result;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class BatchResult {

    private final BatchSummary batchSummary;
    private final List<RecordResult> recordResults;

}
