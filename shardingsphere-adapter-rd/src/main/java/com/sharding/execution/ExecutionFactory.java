package com.sharding.execution;

import com.sharding.SqlSessionFactoryWrapper;
import com.sharding.configuration.TableConfiguration;
import org.apache.ibatis.mapping.SqlCommandType;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: jixd
 * @date: 2020/9/17 2:10 下午
 */
public class ExecutionFactory {

    private final static Map<SqlCommandType, ExecutionStrategy> executionStrategyMap = new HashMap<>(4);


    public ExecutionFactory(SqlSessionFactoryWrapper sqlSessionFactory, Map<String, TableConfiguration> tableConfigurations) throws ClassNotFoundException {
        DMLExecutionStrategy dmlExecutionStrategy = new DMLExecutionStrategy(sqlSessionFactory,tableConfigurations);
        executionStrategyMap.put(SqlCommandType.INSERT,dmlExecutionStrategy);
        executionStrategyMap.put(SqlCommandType.DELETE,dmlExecutionStrategy);
        executionStrategyMap.put(SqlCommandType.UPDATE,dmlExecutionStrategy);
        executionStrategyMap.put(SqlCommandType.SELECT,new DQLExecutionStrategy(sqlSessionFactory,tableConfigurations));
    }

    public ExecutionStrategy getExecutionStrategy(SqlCommandType sqlCommandType){
        return executionStrategyMap.get(sqlCommandType);
    }





}
