# Shardingsphere-Adapter Project
- 项目提供分表时平滑切换方案，适用于 Spring + ShardingJdbc + mybatis技术栈， 
可以平滑分表
- 提供双写和切换读方案的功能
- 提供FlinkJob程序样例，主表数据同步分表，
- 提供雪花算法根据IP生成WorkId方案
- 提供hash算法
- 提供SpringBoot Stater 方便适配SpringBoot项目
## 版本
- SpringBoot 2.1.8.RELEASE
- ShardingSphere 4.0.0
- Mybatis 3.5.2
- Mysql  6.0.6

## 项目结构
- shardingsphere-adapter-rd 提供分片 hash 根据ip生成雪花算法
- shardingsphere-adapter-data Flink 批量数据迁移
- shardingsphere-adapter-bothwriter 提供Sharding jdbc分片迁移后双写方案
- shardingsphere-adapter-spring-boot-starter 适配SpringBoot

## 配置
```
配置ShardingJDBC主master
sharding.adapter.global.master-data-source-name = master
如果自定义SqlSessionFactory配置bean名称
sharding.adapter.global.sql-session-factory-bean-name =
配置分表对应的MapperClass
sharding.adapter.tables.rule.t_user.mapper-class = com.repository.UserMapper
配置分表对应的Resource Xml
sharding.adapter.tables.rule.t_user.mapper-resource = META-INF/mappers/user.xml
定义是否双写
sharding.adapter.tables.rule.t_user.both-writer = true
定义是否读分片表
sharding.adapter.tables.rule.t_user.read-sharding = false
```
## 说明
- 原ShardingJDBC使用的雪花算法无法生成动态WorkId,项目中重写了该部分功能，配置如下
```
spring.shardingsphere.sharding.tables.t_user.key-generator.column = id
spring.shardingsphere.sharding.tables.t_user.key-generator.type = IP_SNOWFLAKE

```

- 有些因为版本差异，MysqlDriver Url中需要添加 参数 nullCatalogMeansCurrent=true&nullNamePatternMatchesAll=true
- FlinkJob入参 --host ip --usrName userName --password password --min 1 --max 10
