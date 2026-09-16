package com.cdq.dataquality.ruleengine.demo.services;

import com.cdq.dataquality.ruleengine.RuleEngine;
import com.cdq.dataquality.ruleengine.result.RuleResult;
import com.cdq.dataquality.ruleengine.rule.Rule;
import com.cdq.dataquality.ruleengine.rule.Status;
import com.cdq.dataquality.ruleengine.result.BatchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RecordsService {

    private final RuleEngine ruleEngine;

    /**
     * Processes records stream via rule-engine.
     * Uses Spring's Webflux .buffer to ensure only requested batch size is loaded to memory at the time.
     * Each batch provides list of results along with summary counters.
     *
     * @param recordsStream    stream of records
     * @param filterByStatus   optional status filter
     * @param filterByCategory optional category filter
     * @param batchSize        batch size
     * @return flux stream of batch results
     */
    public Flux<BatchResult> processRecordsBatchStream(final Flux<Map<String, Object>> recordsStream,
                                                       final Status filterByStatus,
                                                       final String filterByCategory,
                                                       final int batchSize) {
        // only materialise requested batch size at a time
        // return flux so batches can be streamed once they are processed e.g. as server sent events
        return recordsStream.buffer(batchSize)
                .map(records -> this.ruleEngine.processBatch(records, filterByStatus, filterByCategory));
    }

    /**
     * Processes records as a stream.
     * Records are pulled one-by-one and rule results are emitted as soon as rule is executed.
     * One record will emit n amount of rule result events matching number of rules executed.
     *
     * @param recordsStream    stream of records
     * @param filterByStatus   optional status filter
     * @param filterByCategory optional category filter
     * @return flux stream of run events
     */
    public Flux<RuleResult> processRecordsStream(final Flux<Map<String, Object>> recordsStream,
                                                 final Status filterByStatus,
                                                 final String filterByCategory) {
        // load rules to memory, so we don't reload them for each record
        final List<Rule> rules = this.ruleEngine.selectRules(filterByStatus, filterByCategory);

        // use concat map so that rule results are not mixed by different records
        return recordsStream.concatMap(record -> Flux.create(sink -> {
            this.ruleEngine.processSingleRecordStream(record, rules, sink::next);
            sink.complete();
        }));
    }

}
