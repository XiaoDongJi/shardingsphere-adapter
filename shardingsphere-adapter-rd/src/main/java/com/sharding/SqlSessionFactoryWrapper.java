package com.sharding;

import com.sharding.configuration.TableConfiguration;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.logging.slf4j.Slf4jImpl;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.mybatis.spring.transaction.SpringManagedTransactionFactory;
import org.springframework.core.NestedIOException;
import org.springframework.core.io.Resource;
import org.springframework.util.CollectionUtils;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: jixd
 * @date: 2020/9/16 5:30 下午
 */
public class SqlSessionFactoryWrapper {

    private SqlSessionFactory sqlSessionFactory;
    private Map<String, TableConfiguration> tableConfigurations;

    private Map<String,Class> cacheClassMap = new HashMap<>();

    public SqlSessionFactoryWrapper(DataSource dataSource, Map<String, TableConfiguration> tableConfigurations
            , ClassLoader classLoader, TransactionFactory transactionFactory, List<Interceptor> interceptors) throws NestedIOException, ClassNotFoundException {
        this.tableConfigurations = tableConfigurations;
        this.sqlSessionFactory = createSqlSessionFactory(dataSource,tableConfigurations
                ,classLoader,transactionFactory,interceptors);
    }

    private SqlSessionFactory createSqlSessionFactory(DataSource dataSource,Map<String, TableConfiguration> tableConfigurations
            ,ClassLoader classLoader,TransactionFactory transactionFactory,List<Interceptor> interceptors) throws NestedIOException, ClassNotFoundException {
        transactionFactory = transactionFactory == null ? new SpringManagedTransactionFactory() : transactionFactory;
        Environment environment = new Environment("sharding-adapter", transactionFactory, dataSource);
        Configuration configuration = new Configuration(environment);
        configuration.setLazyLoadingEnabled(true);
        configuration.setLogImpl(Slf4jImpl.class);
        if (!CollectionUtils.isEmpty(interceptors)){
            for (Interceptor interceptor : interceptors){
                configuration.addInterceptor(interceptor);
            }
        }
        for(Map.Entry<String,TableConfiguration> entry : tableConfigurations.entrySet()){
            TableConfiguration conf = entry.getValue();
            String mapperClass = conf.getMapperClass();
            try {
                Class<?> aClass = Class.forName(mapperClass, true, classLoader);
                cacheClassMap.put(mapperClass,aClass);
            }catch (ClassNotFoundException e){
                throw e;
            }
            //解析xml资源 注册Mapper，不用在手动注册MapperClass
            if (conf.getMapperResource() != null){
                try {
                    Resource mapperLocation = conf.getMapperResource();
                    XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(mapperLocation.getInputStream(),
                            configuration, mapperLocation.toString(), configuration.getSqlFragments());
                    xmlMapperBuilder.parse();
                } catch (Exception e) {
                    throw new NestedIOException("Failed to parse mapping resource: '" + conf.getMapperResource() + "'", e);
                } finally {
                    ErrorContext.instance().reset();
                }
            }
        }
        SqlSessionFactoryBuilder builder = new SqlSessionFactoryBuilder();
        SqlSessionFactory sqlSessionFactory = builder.build(configuration);
        return sqlSessionFactory;
    }

    public SqlSessionFactory getSqlSessionFactory() {
        return sqlSessionFactory;
    }

    public Map<String, Class> getCacheClassMap() {
        return cacheClassMap;
    }

    public Map<String, TableConfiguration> getTableConfigurations() {
        return tableConfigurations;
    }
}
