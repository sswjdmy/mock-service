package com.vitalgateway.fixserver.fix;

import quickfix.Message;
import quickfix.SessionID;

public interface MessageProcessor {

    void process(Message message, SessionID sessionId);
}
