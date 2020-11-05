package com.sharding.snowflake;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.core.strategy.keygen.TimeService;
import org.apache.shardingsphere.spi.keygen.ShardingKeyGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Properties;
import java.util.Random;

/**
 * copy from org.apache.shardingsphere.core.strategy.keygen.SnowflakeShardingKeyGenerator
 * overwirte getWorkId by ip
 * @author: jixd
 * @date: 2020/10/19 10:21 上午
 */
public class IPWorkIdSnowflakeKeyGenerator implements ShardingKeyGenerator {

    static final Logger logger = LoggerFactory.getLogger(IPWorkIdSnowflakeKeyGenerator.class);

    public static final long EPOCH;

    private static final long SEQUENCE_BITS = 12L;

    private static final long WORKER_ID_BITS = 10L;

    private static final long SEQUENCE_MASK = (1 << SEQUENCE_BITS) - 1;

    private static final long WORKER_ID_LEFT_SHIFT_BITS = SEQUENCE_BITS;

    private static final long TIMESTAMP_LEFT_SHIFT_BITS = WORKER_ID_LEFT_SHIFT_BITS + WORKER_ID_BITS;

    private static final long WORKER_ID_MAX_VALUE = 1L << WORKER_ID_BITS;

    private static final long WORKER_ID;

    private static final int DEFAULT_VIBRATION_VALUE = 1;

    private static final int MAX_TOLERATE_TIME_DIFFERENCE_MILLISECONDS = 10;


    private static TimeService timeService = new TimeService();


    private Properties properties = new Properties();



    private int sequenceOffset = -1;

    private long sequence;

    private long lastMilliseconds;

    static {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2020, Calendar.MARCH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        EPOCH = calendar.getTimeInMillis();
        WORKER_ID = getWorkerId();
    }

    @Override
    public Properties getProperties() {
        return properties;
    }

    @Override
    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    @Override
    public String getType() {
        return "IP_SNOWFLAKE";
    }

    @Override
    public synchronized Comparable<?> generateKey() {
        long currentMilliseconds = timeService.getCurrentMillis();
        if (waitTolerateTimeDifferenceIfNeed(currentMilliseconds)) {
            currentMilliseconds = timeService.getCurrentMillis();
        }
        if (lastMilliseconds == currentMilliseconds) {
            if (0L == (sequence = (sequence + 1) & SEQUENCE_MASK)) {
                currentMilliseconds = waitUntilNextTime(currentMilliseconds);
            }
        } else {
            vibrateSequenceOffset();
            sequence = sequenceOffset;
        }
        lastMilliseconds = currentMilliseconds;
        return ((currentMilliseconds - EPOCH) << TIMESTAMP_LEFT_SHIFT_BITS) | (WORKER_ID << WORKER_ID_LEFT_SHIFT_BITS) | sequence;
    }

    private boolean waitTolerateTimeDifferenceIfNeed(final long currentMilliseconds) {
        if (lastMilliseconds <= currentMilliseconds) {
            return false;
        }
        long timeDifferenceMilliseconds = lastMilliseconds - currentMilliseconds;
        Preconditions.checkState(timeDifferenceMilliseconds < getMaxTolerateTimeDifferenceMilliseconds(),
                "Clock is moving backwards, last time is %d milliseconds, current time is %d milliseconds", lastMilliseconds, currentMilliseconds);
        try {
            Thread.sleep(timeDifferenceMilliseconds);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    private static long getWorkerId() {
        long workerId = getWorkerIdBySimpleIP();
        Preconditions.checkArgument(workerId >= 0L && workerId < WORKER_ID_MAX_VALUE);
        return workerId;
    }


    /**
     * 高4位 = 高8位 ^ 高 16位 ^ 随机数
     * 低8位 = 低8位^ 低16位
     * ID = 高4位 ^ 低8位
     *
     * @return
     */
    private static long getWorkerIdBySimpleIP() {
        InetAddress address;
        try {
            address = InetAddress.getLocalHost();
        } catch (final UnknownHostException e) {
            throw new IllegalStateException("Cannot get LocalHost InetAddress, please check your network!");
        }
        // 得到IP地址的byte[]形式值

        byte[] ipAddressByteArray = address.getAddress();
        long workerId = 0L;
        logger.info("获取ip地址为{}",address.getHostAddress());
        if (ipAddressByteArray.length == 4) {
            Random random = new Random();
            byte randomVal = new Integer(random.nextInt(254) + 1).byteValue();
            int lowVal = (ipAddressByteArray[0] & 0xff) ^ (ipAddressByteArray[1] & 0xff);
            int highVal = (ipAddressByteArray[2] & 0xff) ^ (ipAddressByteArray[3] & 0xff) ^ (randomVal & 0xff);
            workerId = ((highVal & 0x0f) << 4) ^ lowVal;
            logger.info("random={},lowVal={},highVal={},workId={}",randomVal,lowVal,highVal,workerId);
        } else if (ipAddressByteArray.length == 16) {
            for (byte byteNum : ipAddressByteArray) {
                workerId += byteNum & 0B111111;
            }
        } else {
            throw new IllegalStateException("Bad LocalHost InetAddress, please check your network!");
        }

        logger.info("IPWorkIdSnowflakeKeyGenerator workerId,ip:{},workerId:{}",address.getHostAddress(),workerId);
        return workerId;
    }

    private int getMaxVibrationOffset() {
        int result = Integer.parseInt(properties.getProperty("max.vibration.offset", String.valueOf(DEFAULT_VIBRATION_VALUE)));
        Preconditions.checkArgument(result >= 0 && result <= SEQUENCE_MASK, "Illegal max vibration offset");
        return result;
    }

    private int getMaxTolerateTimeDifferenceMilliseconds() {
        return Integer.valueOf(properties.getProperty("max.tolerate.time.difference.milliseconds", String.valueOf(MAX_TOLERATE_TIME_DIFFERENCE_MILLISECONDS)));
    }

    private long waitUntilNextTime(final long lastTime) {
        long result = timeService.getCurrentMillis();
        while (result <= lastTime) {
            result = timeService.getCurrentMillis();
        }
        return result;
    }

    private void vibrateSequenceOffset() {
        sequenceOffset = sequenceOffset >= getMaxVibrationOffset() ? 0 : sequenceOffset + 1;
    }

    public static void main(String[] args) throws UnknownHostException {
        for (int i = 0;i < 1000;i++){
            System.out.println(getWorkerIdBySimpleIP());
        }
    }

}
