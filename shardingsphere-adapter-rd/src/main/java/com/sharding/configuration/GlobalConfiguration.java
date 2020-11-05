package com.sharding.configuration;


/**
 * global configuration
 * @author: jixd
 * @date: 2020/9/16 5:11 下午
 */
public class GlobalConfiguration {

    private String dataSourceName;

    private String sqlSessionFactoryBeanName;

    public String getMasterDataSourceName() {
        return dataSourceName;
    }

    public void setMasterDataSourceName(String masterDataSourceName) {
        this.dataSourceName = masterDataSourceName;
    }

    public String getSqlSessionFactoryBeanName() {
        return sqlSessionFactoryBeanName;
    }

    public void setSqlSessionFactoryBeanName(String sqlSessionFactoryBeanName) {
        this.sqlSessionFactoryBeanName = sqlSessionFactoryBeanName;
    }
}
