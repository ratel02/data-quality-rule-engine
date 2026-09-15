package com.cdq.dataquality.ruleengine.demo.controllers;

import com.cdq.dataquality.ruleengine.rule.Status;
import com.cdq.dataquality.ruleengine.demo.models.responses.BatchProcessingResponse;
import com.cdq.dataquality.ruleengine.demo.services.BatchService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
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
public class BatchController {

    private final BatchService batchService;

    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
            mediaType = APPLICATION_JSON_VALUE,
            schema = @Schema(type = "string"),
            examples = @ExampleObject(name = "Example records", externalValue = "/examples/records.json")
    ))
    @PostMapping(value = "/batch-demo", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public Flux<BatchProcessingResponse> processBatchCommand(@RequestBody Flux<Map<String, Object>> records,
                                                             @RequestParam(required = false) Status filterByStatus,
                                                             @RequestParam(required = false) String filterByCategory,
                                                             @RequestParam(defaultValue = "1000") int batchSize) {
        return this.batchService.processRecordsStream(records, filterByStatus, filterByCategory, batchSize)
                .map(batchResult -> new BatchProcessingResponse(
                        batchResult.getBatchSummary(),
                        batchResult.getRecordResults()
                ));
    }

}
