package com.sharding;

import com.sharding.configuration.TableConfiguration;
import com.sharding.execution.ExecutionFactory;
import com.sharding.execution.ExecutionStrategy;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Invocation;

import java.util.Objects;

/**
 * Spring bean注入 Interceptor 处理
 *
 * @author: jixd
 * @date: 2020/9/17 1:59 下午
 */

public class Executions {

    private ExecutionFactory executionFactory;
    private SqlSessionFactoryWrapper sqlSessionFactoryWrapper;

    public Executions(SqlSessionFactoryWrapper sqlSessionFactoryWrapper) {
        this.sqlSessionFactoryWrapper = sqlSessionFactoryWrapper;
        try {
            this.executionFactory = new ExecutionFactory(sqlSessionFactoryWrapper,sqlSessionFactoryWrapper.getTableConfigurations());
        } catch (ClassNotFoundException e) {
            //防御性容错
        }
    }

    public Object proxyExecute(Invocation invocation) throws Exception {
        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        SqlCommandType sqlCommandType = ms.getSqlCommandType();
        String id = ms.getId();
        String interfaceMapper = id.substring(0, id.lastIndexOf("."));
        TableConfiguration tableConfiguration = sqlSessionFactoryWrapper.getTableConfigurations().get(interfaceMapper);
        //表未做配置放行
        if (Objects.isNull(tableConfiguration) || executionFactory == null) {
            return invocation.proceed();
        }

        ExecutionStrategy executionStrategy = executionFactory.getExecutionStrategy(sqlCommandType);
        if (Objects.isNull(executionStrategy)){
            return invocation.proceed();
        }
        return executionStrategy.execute(invocation);
    }

}
