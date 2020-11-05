package com.sharding.adapter.spring.boot;

import com.sharding.Executions;
import com.sharding.ShardingAdapterInterceptor;
import com.sharding.SqlSessionFactoryWrapper;
import com.sharding.configuration.TableConfiguration;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.ShardingDataSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.core.NestedIOException;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: jixd
 * @date: 2020/9/17 9:49 上午
 */
@Configuration
@ConditionalOnProperty(prefix = "sharding.adapter", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties({GlobalConfigurationProperties.class,TableConfigurationProperties.class})
@ConditionalOnClass(name = "org.apache.shardingsphere.shardingjdbc.spring.boot.SpringBootConfiguration")
@AutoConfigureAfter(name = {"org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration","org.apache.shardingsphere.shardingjdbc.spring.boot.SpringBootConfiguration"})
public class ShardingAdapterAutoConfiguration implements BeanFactoryAware, BeanClassLoaderAware {

    private GlobalConfigurationProperties globalConfigurationProperties;
    private TableConfigurationProperties tableConfigurationProperties;

    public ShardingAdapterAutoConfiguration(GlobalConfigurationProperties globalConfigurationProperties, TableConfigurationProperties tableConfigurationProperties) {
        this.globalConfigurationProperties = globalConfigurationProperties;
        this.tableConfigurationProperties = tableConfigurationProperties;
    }

    private BeanFactory beanFactory;
    private ClassLoader classLoader;


    @Bean
    @Conditional(ExecutionCondition.class)
    public Executions executions() throws NestedIOException, ClassNotFoundException {
        Executions executions = createExecution();
        buildInterceptor(executions);
        return executions;
    }

    public void buildInterceptor(Executions executions){
        ShardingAdapterInterceptor shardingAdapterInterceptor = new ShardingAdapterInterceptor();
        shardingAdapterInterceptor.setExecutions(executions);
        SqlSessionFactory sqlSessionFactory = getMasterSqlSessionFactory();
        sqlSessionFactory.getConfiguration().addInterceptor(shardingAdapterInterceptor);
    }

    private SqlSessionFactory getMasterSqlSessionFactory(){
        SqlSessionFactory sqlSessionFactory ;
        if (StringUtils.isEmpty(globalConfigurationProperties.getSqlSessionFactoryBeanName())){
            sqlSessionFactory = beanFactory.getBean(SqlSessionFactory.class);
        }else{
            sqlSessionFactory = beanFactory.getBean(globalConfigurationProperties.getSqlSessionFactoryBeanName(),SqlSessionFactory.class);
        }
        return sqlSessionFactory;
    }


    private Executions createExecution() throws NestedIOException, ClassNotFoundException {
        ShardingDataSource dataSource = beanFactory.getBean(ExecutionCondition.sharding_name, ShardingDataSource.class);
        DataSource realDataSource = dataSource.getDataSourceMap().get(globalConfigurationProperties.getMasterDataSourceName());
        SqlSessionFactory sqlSessionFactory = getMasterSqlSessionFactory();
        TransactionFactory transactionFactory = sqlSessionFactory.getConfiguration().getEnvironment().getTransactionFactory();
        List<Interceptor> interceptors = sqlSessionFactory.getConfiguration().getInterceptors();
        Map<String, TableConfiguration> stringTableConfigurationMap = transferRuleMap(tableConfigurationProperties.getRule());
        SqlSessionFactoryWrapper sqlSessionFactoryWrapper = new SqlSessionFactoryWrapper(realDataSource,stringTableConfigurationMap,classLoader,transactionFactory,interceptors);
        return new Executions(sqlSessionFactoryWrapper);
    }

    private Map<String, TableConfiguration> transferRuleMap(Map<String, TableConfiguration> tableConfigurations) {
        Map<String,TableConfiguration> rules = new HashMap<>();
        for (Map.Entry<String,TableConfiguration> entry : tableConfigurations.entrySet()){
            String mapperClass = entry.getValue().getMapperClass();
            rules.put(mapperClass,entry.getValue());
        }
        return rules;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }


}
