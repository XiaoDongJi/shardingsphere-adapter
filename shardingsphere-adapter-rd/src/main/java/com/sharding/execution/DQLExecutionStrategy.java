package com.sharding.execution;

import com.sharding.SqlSessionFactoryWrapper;
import com.sharding.configuration.TableConfiguration;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Invocation;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * query
 * @author: jixd
 * @date: 2020/9/17 1:47 下午
 */
public class DQLExecutionStrategy extends AbstractExecutionStrategy{

    public DQLExecutionStrategy(SqlSessionFactoryWrapper sqlSessionFactoryWrapper, Map<String, TableConfiguration> tableConfigurations) {
        super(sqlSessionFactoryWrapper, tableConfigurations);
    }

    @Override
    protected Object doExecute(Invocation invocation, TableConfiguration tableRule) throws InvocationTargetException, IllegalAccessException {
        Object rest = null;
        if (tableRule.getReadSharding()){
            rest = invocation.proceed();
        }else{
            Object[] args = invocation.getArgs();
            try {
                rest = invoke((MappedStatement) args[0], args[1]);
                if (rest != null && !(List.class.isAssignableFrom(rest.getClass()))){
                    List<Object> tmp = new ArrayList<>();
                    tmp.add(rest);
                    rest = tmp;
                }
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("query exception ",e);
            }
        }
        return rest;
    }

}
