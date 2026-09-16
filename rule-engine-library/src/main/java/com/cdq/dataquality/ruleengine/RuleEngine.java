package com.cdq.dataquality.ruleengine;

import com.cdq.dataquality.ruleengine.catalog.RulesCatalog;
import com.cdq.dataquality.ruleengine.processor.RulesProcessor;
import com.cdq.dataquality.ruleengine.result.BatchSummary;
import com.cdq.dataquality.ruleengine.result.RecordResult;
import com.cdq.dataquality.ruleengine.rule.Decision;
import com.cdq.dataquality.ruleengine.rule.Rule;
import com.cdq.dataquality.ruleengine.result.RuleResult;
import com.cdq.dataquality.ruleengine.rule.Severity;
import com.cdq.dataquality.ruleengine.rule.Status;
import com.cdq.dataquality.ruleengine.result.BatchResult;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static com.cdq.dataquality.ruleengine.rule.Status.RELEASED;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

/**
 * A main class to be used - detached from any specific technology, so that it can be used in:
 * - http service
 * - batch worker
 * - scheduled job
 * Regardless of the technology chosen e.g. Spring Webflux, Parallel Collectors, RxJava, etc.
 */
@RequiredArgsConstructor
public class RuleEngine {

    public static final String MISSING_ID_ERROR = "Record ID not found!";

    private final RulesProcessor rulesProcessor;
    private final RulesCatalog rulesCatalog;

    /**
     * Selects rules by given filters.
     *
     * @param filterByStatus   status filter (currently unused as requested by functional requirement)
     * @param filterByCategory category filter
     * @return rules that match criteria
     */
    public List<Rule> selectRules(final Status filterByStatus, final String filterByCategory) {
        // get rules, apply filters
        return this.rulesCatalog.getRules()
                .stream()
                // intentionally commented out and replaced with RELEASED filter as per F6.5 functional requirement
                // .filter(rule -> isNull(filterByStatus) || filterByStatus == rule.getStatus())
                .filter(rule -> rule.getStatus() == RELEASED)
                .filter(rule -> isNull(filterByCategory) || (nonNull(rule.getCategories()) && rule.getCategories().contains(filterByCategory)))
                .toList();
    }

    /**
     * Processes a batch of records.
     * It's a convenience/materialised API intended for bounded batches.
     * For a one-by-one streaming see {@link this#processSingleRecord(Map, List)}
     *
     * @param records          records batch
     * @param filterByStatus   optional filter by status
     * @param filterByCategory optional filter by category
     * @return batch records processing result
     */
    public BatchResult processBatch(final List<Map<String, Object>> records,
                                    final Status filterByStatus,
                                    final String filterByCategory) {
        // select rules
        final List<Rule> rules = this.selectRules(filterByStatus, filterByCategory);

        // process batch of records (materialised)
        final List<RecordResult> results = records.stream()
                .map(record -> this.processSingleRecord(record, rules))
                .toList();

        // get success and failed results
        final int successResults = results.stream()
                .map(recordResult -> recordResult.getRuleResultsSuccess().size())
                .reduce(0, Integer::sum);
        final int failedResults = results.stream()
                .map(recordResult -> recordResult.getRuleResultsFailure().size())
                .reduce(0, Integer::sum);

        // grouping by decision: count how much success rule results each record result has
        final Map<Decision, Long> resultsByDecision = results.stream()
                .flatMap(recordResult -> recordResult.getRuleResultsSuccess()
                        .stream()
                        .map(ruleResult -> Map.entry(ruleResult.getDecision(), recordResult)))
                .collect(groupingBy(Map.Entry::getKey, counting()));

        // grouping by severity: count how much success rule results each record result has
        final Map<Severity, Long> resultsBySeverity = results.stream()
                .flatMap(recordResult -> recordResult.getRuleResultsSuccess()
                        .stream()
                        .map(ruleResult -> Map.entry(ruleResult.getSeverity(), recordResult)))
                .collect(groupingBy(Map.Entry::getKey, counting()));

        // build and return summary response
        return new BatchResult(new BatchSummary(successResults, failedResults, resultsByDecision, resultsBySeverity), results);
    }

    /**
     * Processes single record.
     *
     * @param record record to be processed
     * @param rules  all rules that apply
     * @return record result class
     */
    private RecordResult processSingleRecord(final Map<String, Object> record, final List<Rule> rules) {
        // process rules
        final List<RuleResult> ruleResults = this.rulesProcessor.processRules(record, rules);

        // group by success
        final Map<Boolean, List<RuleResult>> resultsBySuccess = ruleResults.stream()
                .collect(groupingBy(RuleResult::isSuccess));

        // build result
        return new RecordResult(
                UUID.randomUUID(),
                Optional.ofNullable(record.get("id"))
                        .map(String::valueOf)
                        .orElse(MISSING_ID_ERROR),
                resultsBySuccess.getOrDefault(true, emptyList()),
                resultsBySuccess.getOrDefault(false, emptyList())
        );
    }

    /**
     * Processes single record in using {@link RulesProcessor#processRulesStream(Map, List, Consumer)} method.
     * This does not accumulate any data in memory (event batch) and only one record is processed at a time.
     * Also, every rule result is emitted straight away using ruleResultConsumer param.
     *
     * @param record             record to be processed
     * @param rules              all rules that apply
     * @param ruleResultConsumer a consumer that accepts rule processed event
     *
     */
    public void processSingleRecordStream(final Map<String, Object> record,
                                          final List<Rule> rules,
                                          final Consumer<RuleResult> ruleResultConsumer) {
        // process rules
        this.rulesProcessor.processRulesStream(record, rules, ruleResultConsumer);
    }

}
