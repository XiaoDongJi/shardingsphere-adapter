package com.sharding.adapter.spring.boot;

import com.sharding.configuration.TableConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: jixd
 * @date: 2020/9/17 10:27 上午
 */
@ConfigurationProperties(prefix = "sharding.adapter.tables")
public class TableConfigurationProperties {

    private Map<String, TableConfiguration> rule = new HashMap<>();

    public Map<String, TableConfiguration> getRule() {
        return rule;
    }

    public void setRule(Map<String, TableConfiguration> rule) {
        this.rule = rule;
    }
}
