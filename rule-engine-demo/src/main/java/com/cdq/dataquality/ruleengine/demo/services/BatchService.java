package com.cdq.dataquality.ruleengine.demo.services;

import com.cdq.dataquality.ruleengine.RuleEngine;
import com.cdq.dataquality.ruleengine.rule.Status;
import com.cdq.dataquality.ruleengine.result.BatchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class BatchService {

    private final RuleEngine ruleEngine;

    /**
     * Processes records stream via rule-engine.
     * Uses Spring's Webflux .buffer to ensure only requested batch size is loaded to memory at the time.
     *
     * @param recordsStream        stream of records
     * @param filterByStatus       optional status filter
     * @param filterByCategory     optional category filter
     * @param batchSize            batch size
     * @return flux stream of batch results
     */
    public Flux<BatchResult> processRecordsStream(final Flux<Map<String, Object>> recordsStream,
                                                  final Status filterByStatus,
                                                  final String filterByCategory,
                                                  final int batchSize) {
        // only materialise requested batch size at a time
        // return flux so batches can be streamed e.g. as server sent events
        return recordsStream.buffer(batchSize)
                .map(records -> this.ruleEngine.processBatch(records, filterByStatus, filterByCategory));
    }

}
