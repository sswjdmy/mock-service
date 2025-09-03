package com.vitalgateway.fixserver.fix;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import quickfix.Message;
import quickfix.SessionID;

import java.util.Map;
import java.util.concurrent.Executors;

@Service
public class FromAppMessageProcessor {

    @Autowired
    Map<String, MessageProcessor> strategyMap;

    public void process(Message message, SessionID sessionId) {

        try {
            String msgType = message.getHeader().getString(quickfix.field.MsgType.FIELD);
            if (strategyMap.containsKey(msgType)) {
                strategyMap.get(msgType).process(message, sessionId);
//                try (var exec =  Executors.newVirtualThreadPerTaskExecutor()) {
//                    exec.submit(() -> strategyMap.get(msgType).process(message, sessionId));
//                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
