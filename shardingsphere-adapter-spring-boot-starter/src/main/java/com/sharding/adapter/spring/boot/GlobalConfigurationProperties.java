package com.sharding.adapter.spring.boot;

import com.sharding.configuration.GlobalConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author: jixd
 * @date: 2020/9/17 10:19 上午
 */
@ConfigurationProperties(prefix = "sharding.adapter.global")
public class GlobalConfigurationProperties extends GlobalConfiguration {

}
