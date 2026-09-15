package com.cdq.dataquality.ruleengine.demo.models.responses;

import com.cdq.dataquality.ruleengine.result.BatchSummary;
import com.cdq.dataquality.ruleengine.result.RecordResult;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class BatchProcessingResponse {

    private final BatchSummary batchSummary;
    private final List<RecordResult> recordResults;

}
