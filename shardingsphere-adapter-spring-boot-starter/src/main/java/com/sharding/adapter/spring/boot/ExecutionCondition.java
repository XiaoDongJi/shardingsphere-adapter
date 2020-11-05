package com.sharding.adapter.spring.boot;

import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * 执行器组装条件
 * @author: jixd
 * @date: 2020/9/17 4:57 下午
 */
public class ExecutionCondition extends SpringBootCondition {

    public static final String sharding_name = "shardingDataSource";

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return context.getBeanFactory().containsBean(sharding_name) ? ConditionOutcome.match() : ConditionOutcome.noMatch("has not bean shardingDataSource");
    }
}
