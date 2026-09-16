package com.cdq.dataquality.ruleengine.demo.controllers;

import com.cdq.dataquality.ruleengine.result.RuleResult;
import com.cdq.dataquality.ruleengine.rule.Status;
import com.cdq.dataquality.ruleengine.demo.models.responses.BatchProcessingResponse;
import com.cdq.dataquality.ruleengine.demo.services.RecordsService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequiredArgsConstructor
public class RecordsController {

    private final RecordsService recordsService;

    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
            mediaType = APPLICATION_JSON_VALUE,
            examples = @ExampleObject(name = "Example records", externalValue = "/examples/records.json")
    ))
    @PostMapping(value = "/batch-demo", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public Flux<BatchProcessingResponse> processRecordsBatch(@RequestBody Flux<Map<String, Object>> records,
                                                             @RequestParam(required = false) Status filterByStatus,
                                                             @RequestParam(required = false) String filterByCategory,
                                                             @RequestParam(defaultValue = "1000") int batchSize) {
        return this.recordsService.processRecordsBatchStream(records, filterByStatus, filterByCategory, batchSize)
                .map(batchResult -> new BatchProcessingResponse(
                        batchResult.getBatchSummary(),
                        batchResult.getRecordResults()
                ));
    }

    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
            mediaType = APPLICATION_JSON_VALUE,
            examples = @ExampleObject(name = "Example records", externalValue = "/examples/records.json")
    ))
    @PostMapping(value = "/stream-demo", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public Flux<RuleResult> processRecordsStream(@RequestBody Flux<Map<String, Object>> records,
                                                 @RequestParam(required = false) Status filterByStatus,
                                                 @RequestParam(required = false) String filterByCategory) {
        return this.recordsService.processRecordsStream(records, filterByStatus, filterByCategory);
    }

}
