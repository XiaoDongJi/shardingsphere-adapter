package com.sharding.algorithm;

import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;

import java.util.Collection;

/**
 * hash 配置
 * @author: jixd
 * @date: 2020/9/15 2:53 下午
 */
public class KeyPreciseShardingAlgorithm implements PreciseShardingAlgorithm<String> {
    @Override
    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<String> shardingValue) {

        String value = shardingValue.getValue();
        int size = availableTargetNames.size();
        int index = Hash.fnv1_32_hash(value) % size;
        for (String target : availableTargetNames){
            if (target.endsWith(String.valueOf(index))){
                return target;
            }
        }
        return shardingValue.getLogicTableName();
    }
}
