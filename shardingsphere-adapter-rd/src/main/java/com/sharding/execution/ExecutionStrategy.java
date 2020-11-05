package com.sharding.execution;

import org.apache.ibatis.plugin.Invocation;

/**
 * 执行策略
 *
 * @author: jixd
 * @date: 2020/9/17 10:58 上午
 */
public interface ExecutionStrategy {

    Object execute(Invocation invocation) throws Exception;


}
