package com.sharding.configuration;

import org.springframework.core.io.Resource;

/**
 *  table config
 * @author: jixd
 * @date: 2020/9/16 4:26 下午
 */
public class TableConfiguration {

    private String mapperClass;

    private Resource mapperResource;

    private Boolean isBothWriter;

    private Boolean readSharding;


    public String getMapperClass() {
        return mapperClass;
    }

    public void setMapperClass(String mapperClass) {
        this.mapperClass = mapperClass;
    }

    public Resource getMapperResource() {
        return mapperResource;
    }

    public void setMapperResource(Resource mapperResource) {
        this.mapperResource = mapperResource;
    }

    public Boolean getBothWriter() {
        return isBothWriter;
    }

    public void setBothWriter(Boolean bothWriter) {
        isBothWriter = bothWriter;
    }

    public Boolean getReadSharding() {
        return readSharding;
    }

    public void setReadSharding(Boolean readSharding) {
        this.readSharding = readSharding;
    }

}
