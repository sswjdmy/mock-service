package com.vitalgateway.fixserver.fix;


import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import quickfix.Message;
import quickfix.SessionID;
import quickfix.fix50sp2.MarketDataIncrementalRefresh;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service(MarketDataIncrementalRefresh.MSGTYPE)
public class MarketDataIncrementalRefreshProcessor implements MessageProcessor {

    private static final DateTimeFormatter yyyyMMdd = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter HHmmss = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    @Override
    @SneakyThrows
    public void process(Message message, SessionID sessionId) {
        // 获取报价消息的时间
        String dateStr = message.getString(272);
        String timeStr = message.getString(273);

        LocalDate date = LocalDate.parse(dateStr, yyyyMMdd);
        LocalTime time = LocalTime.parse(timeStr, HHmmss);

        LocalDateTime localDateTime = LocalDateTime.of(date, time).plusHours(8);

        // 获取毫秒
        long epochMilli = localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        log.info("check time delay: {} MS", System.currentTimeMillis() - epochMilli);
    }
}
