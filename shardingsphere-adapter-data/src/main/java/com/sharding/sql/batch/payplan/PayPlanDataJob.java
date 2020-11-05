package com.sharding.sql.batch.payplan;


import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.RowTypeInfo;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.connector.jdbc.JdbcConnectionOptions;
import org.apache.flink.connector.jdbc.JdbcExecutionOptions;
import org.apache.flink.connector.jdbc.JdbcInputFormat;
import org.apache.flink.connector.jdbc.JdbcSink;
import org.apache.flink.connector.jdbc.split.JdbcNumericBetweenParametersProvider;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.types.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Types;
import java.util.Date;

/**
 * sql 批量迁移
 *
 * @author: jixd
 * @date: 2020/9/18 3:45 下午
 */
public class PayPlanDataJob {
    static final Logger logger = LoggerFactory.getLogger(PayPlanDataJob.class);
    public static void main(String[] args) throws Exception {



        ParameterTool parameter = ParameterTool.fromArgs(args);
        String host = parameter.getRequired("host"); //10.16.16.14
        String userName = parameter.get("usrName"); //dev_crm
        String password = parameter.get("password"); //ziroomdb
        long min = parameter.getLong("min");
        long max = parameter.getLong("max");


        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        TypeInformation<?>[] fieldTypes = new TypeInformation<?>[]{
                BasicTypeInfo.LONG_TYPE_INFO, //id
                BasicTypeInfo.STRING_TYPE_INFO, //contract_code
                BasicTypeInfo.INT_TYPE_INFO, //bill_year
                BasicTypeInfo.BIG_DEC_TYPE_INFO, //current_year_origin_price
                BasicTypeInfo.BIG_DEC_TYPE_INFO,//current_year_price
                BasicTypeInfo.BIG_DEC_TYPE_INFO,//current_year_origin_commission
                BasicTypeInfo.BIG_DEC_TYPE_INFO,//current_year_commission
                BasicTypeInfo.DATE_TYPE_INFO,//bill_start_date
                BasicTypeInfo.DATE_TYPE_INFO,//bill_end_date
                BasicTypeInfo.DATE_TYPE_INFO,//pre_collection_date
                BasicTypeInfo.DATE_TYPE_INFO,//real_collection_date
                BasicTypeInfo.INT_TYPE_INFO,//period
                BasicTypeInfo.STRING_TYPE_INFO,//cost_code
                BasicTypeInfo.BIG_DEC_TYPE_INFO,//origin_fee
                BasicTypeInfo.BIG_DEC_TYPE_INFO,//fee
                BasicTypeInfo.STRING_TYPE_INFO,//bill_num
                BasicTypeInfo.DATE_TYPE_INFO,//create_time
                BasicTypeInfo.DATE_TYPE_INFO,//last_modify_time
                BasicTypeInfo.BOOLEAN_TYPE_INFO,//is_del

        };

        RowTypeInfo rowTypeInfo = new RowTypeInfo(fieldTypes);

        DataStreamSource source = env.createInput(
                JdbcInputFormat.buildJdbcInputFormat()
                        .setDrivername("com.mysql.cj.jdbc.Driver")
                        .setDBUrl("jdbc:mysql://" + host + "/db?useSSL=false&useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&zeroDateTimeBehavior=convertToNull")
                        .setUsername(userName)
                        .setPassword(password)
                        .setQuery("SELECT * FROM t_user WHERE id BETWEEN ? AND ?")
                        .setFetchSize(1000)
                        .setRowTypeInfo(rowTypeInfo)
                        .setParametersProvider(new JdbcNumericBetweenParametersProvider(1000, min, max))
                        .setResultSetConcurrency(1)
                        .finish());

        for (int i = 0; i <= 15; i++) {
            addSource(source, i, host, userName, password);
        }

        env.execute("PayPlanDataJob");

    }

    private static void addSource(DataStreamSource source, final int index, String host, String userName, String password) {

        source.filter(new FilterFunction<Row>() {
            @Override
            public boolean filter(Row row) throws Exception {
                String contractCode = (String) row.getField(1);
                if (Hash.fnv1_32_hash(contractCode) % 16 == index) return true;
                else return false;
            }
        }).addSink(JdbcSink.<Row>sink("INSERT INTO t_user_" + index + " (id, contract_code, bill_year, current_year_origin_price, current_year_price, current_year_origin_commission, current_year_commission, bill_start_date, bill_end_date, pre_collection_date, real_collection_date, period, cost_code, origin_fee, fee, bill_num, create_time, last_modify_time, is_del) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", (ps, t) -> {
                    logger.info("table index={},id={},contractCode={}",index,t.getField(0),t.getField(1));
                    ps.setLong(1, (Long) t.getField(0));
                    ps.setString(2, (String) t.getField(1));
                    if (t.getField(2) == null) ps.setNull(3, Types.INTEGER) ;else ps.setInt(3, (Integer) t.getField(2));
                    ps.setBigDecimal(4, (BigDecimal) t.getField(3));
                    ps.setBigDecimal(5, (BigDecimal) t.getField(4));
                    ps.setBigDecimal(6, (BigDecimal) t.getField(5));
                    ps.setBigDecimal(7, (BigDecimal) t.getField(6));
                    ps.setDate(8, toDate(t.getField(7)));
                    ps.setDate(9, toDate(t.getField(8)));
                    ps.setDate(10, toDate(t.getField(9)));
                    if (t.getField(10) == null) ps.setNull(11, Types.DATE) ; else ps.setDate(11, toDate(t.getField(10)));
                    if (t.getField(11) == null) ps.setNull(12,Types.INTEGER); else ps.setInt(12, (Integer) t.getField(11));
                    ps.setString(13, (String) t.getField(12));
                    ps.setBigDecimal(14, (BigDecimal) t.getField(13));
                    ps.setBigDecimal(15, (BigDecimal) t.getField(14));
                    ps.setString(16, (String) t.getField(15));
                    ps.setDate(17, toDate(t.getField(16)));
                    ps.setDate(18, toDate(t.getField(17)));
                    ps.setBoolean(19, (Boolean) t.getField(18));
                }, JdbcExecutionOptions.builder().withBatchSize(200).build(),
                new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
                        .withDriverName("com.mysql.cj.jdbc.Driver")
                        .withUsername(userName)
                        .withPassword(password)
                        .withUrl("jdbc:mysql://" + host + "/db?useSSL=false&useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&zeroDateTimeBehavior=convertToNull")
                        .build()))
                .name("table_" + index)
                .setParallelism(2);
    }


    private static java.sql.Date toDate(Object o) {
        if (o == null){
            return null;
        }
        Date date = (Date) o;
        java.sql.Date nDate = new java.sql.Date(date.getTime());
        return nDate;
    }

}

