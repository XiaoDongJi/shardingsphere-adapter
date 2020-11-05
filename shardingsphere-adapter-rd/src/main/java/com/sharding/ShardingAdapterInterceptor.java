package com.sharding;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.util.Objects;
import java.util.Properties;

/**
 * mybatis {@Link Interceptor }
 * @author: jixd
 * @date: 2020/9/16 4:10 下午
 */
@Intercepts(
        {
                @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
                @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
                @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),
        }
)
public class ShardingAdapterInterceptor implements Interceptor {
    //执行器 spring方式注入
    private Executions executions;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        if (Objects.isNull(executions)) return invocation.proceed();
        return executions.proxyExecute(invocation);
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        //do nothing
    }

    public void setExecutions(Executions executions) {
        this.executions = executions;
    }
}
