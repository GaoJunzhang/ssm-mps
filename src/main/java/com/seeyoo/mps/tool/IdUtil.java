package com.seeyoo.mps.tool;

import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentityGenerator;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLongArray;

@Component
@Slf4j
public class IdUtil extends IdentityGenerator {
    private static final long MAX_MACHINE_ID = 1023L;
    private static final int CAPACITY = 200;
    private static final int TIMESTAMP_SHIFT_COUNT = 22;
    private static final int MACHINE_ID_SHIFT_COUNT = 12;
    private static final long SEQUENCE_MASK = 4095L;
    private static long START_THE_WORLD_MILLIS;
    private AtomicLongArray messageIdCycle = new AtomicLongArray(CAPACITY);
    private long machineId;

    static {
        try {
            //使用一个固定的时间作为start the world的初始值
            START_THE_WORLD_MILLIS = DateUtil.parse("2019-10-29 00:00:00").getTime();
        } catch (Exception e) {
            throw new RuntimeException("init start the world millis failed", e);
        }
    }

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) {
        Object id = getNextId();
        if (id != null) {
            return (Serializable) id;
        }

        return super.generate(session, object);
    }

    private long getNextId() {
        if (machineId == 0) {
            machineId = getMachineId();
            log.info("machine id:{}", machineId);
        }
        do {
            long timestamp = System.currentTimeMillis() - START_THE_WORLD_MILLIS;
            int index = (int) (timestamp % CAPACITY);
            long messageIdInCycle = messageIdCycle.get(index);
            long timestampInCycle = messageIdInCycle >> TIMESTAMP_SHIFT_COUNT;
            if (messageIdInCycle == 0 || timestampInCycle < timestamp) {
                long messageId = timestamp << TIMESTAMP_SHIFT_COUNT | machineId << MACHINE_ID_SHIFT_COUNT;
                if (messageIdCycle.compareAndSet(index, messageIdInCycle, messageId)) {
                    return messageId;
                }
                log.info("messageId cycle CAS1 failed");
            }
            // 如果当前时间戳与messageIdCycle的时间戳相等，使用环中的序列号+1的方式，生成新的序列号
            // 如果发生了时间回退的情况，（即timestampInCycle > timestamp的情况）那么不能也更新messageIdCycle 的时间戳，使用Cycle中MessageId+1
            if (timestampInCycle >= timestamp) {
                long sequence = messageIdInCycle & SEQUENCE_MASK;
                if (sequence >= SEQUENCE_MASK) {
                    log.info("over sequence mask :{}", sequence);
                    continue;
                }
                long messageId = messageIdInCycle + 1L;
                // 使用CAS的方式保证在该条件下，messageId 不被重复
                if (messageIdCycle.compareAndSet(index, messageIdInCycle, messageId)) {
                    return messageId;
                }
                log.info("messageId cycle CAS2 failed");
            }
            // 整个生成过程中，采用的spinLock
        } while (true);
    }

    private long getMachineId() {
        try {
            String mac = CommonUtil.getMac();
            if (mac.length() > 2) {
                long id = Long.valueOf(mac.substring(mac.length() - 2), 16);
                if (id == 0)
                    id = 300l;
                return id;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1000l;
    }
}
