package com.sharding.execution;

import com.sharding.SqlSessionFactoryWrapper;
import com.sharding.configuration.TableConfiguration;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Invocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * insert delete update
 * @author: jixd
 * @date: 2020/9/17 1:47 下午
 */
public class DMLExecutionStrategy extends AbstractExecutionStrategy{
    Logger logger = LoggerFactory.getLogger(getClass());

    public DMLExecutionStrategy(SqlSessionFactoryWrapper sqlSessionFactoryWrapper, Map<String, TableConfiguration> tableConfigurations) {
        super(sqlSessionFactoryWrapper, tableConfigurations);
    }


    @Override
    protected Object doExecute(Invocation invocation, TableConfiguration tableRule) throws InvocationTargetException, IllegalAccessException {

        Object proceed = invocation.proceed();
        if (tableRule.getBothWriter()){
            try {
                //执行双写  //如果配置异步 开启线程处理
                Object[] args = invocation.getArgs();
                Object res = invoke((MappedStatement) args[0], args[1]);
                logger.info("invoke result {}",res);
            }catch (Exception e){
                logger.error("both write exception",e);
                throw new RuntimeException("main table write exception ",e);
            }

        }

        return proceed;
    }

}
