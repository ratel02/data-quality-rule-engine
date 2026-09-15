package com.cdq.dataquality.ruleengine.demo.configurations;

import com.cdq.dataquality.ruleengine.RuleEngine;
import com.cdq.dataquality.ruleengine.catalog.InMemoryStaticRulesCatalog;
import com.cdq.dataquality.ruleengine.catalog.RulesCatalog;
import com.cdq.dataquality.ruleengine.processor.RulesProcessor;
import com.cdq.dataquality.ruleengine.processor.SimpleRulesProcessor;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;

@SpringBootConfiguration
public class RuleEngineConfiguration {

    @Bean
    public RulesCatalog rulesCatalog() {
        return new InMemoryStaticRulesCatalog();
    }

    @Bean
    public RulesProcessor rulesProcessor() {
        return new SimpleRulesProcessor();
    }

    @Bean
    public RuleEngine ruleEngine(RulesCatalog rulesCatalog, RulesProcessor rulesProcessor) {
        return new RuleEngine(rulesProcessor, rulesCatalog);
    }

}
