package com.vitalgateway.fixserver;

import com.vitalgateway.fixserver.fix.FromAppMessageProcessor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import quickfix.Application;
import quickfix.FieldNotFound;
import quickfix.IncorrectTagValue;
import quickfix.Message;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.UnsupportedMessageType;
import quickfix.field.*;
import quickfix.fix44.ExecutionReport;
import quickfix.fix44.MessageCracker;
import quickfix.fix44.NewOrderSingle;

import java.time.LocalDateTime;


@Slf4j
@Primary
@Component
@RequiredArgsConstructor
public class ServerApplicationAdapter  implements Application  {

    private final FromAppMessageProcessor fromAppMessageProcessor;

    @Override
    public void fromAdmin(Message message, SessionID sessionId) {
        log.info("fromAdmin: Message={}, SessionId={}", message, sessionId);
    }

    @Override
    public void fromApp(Message message, SessionID sessionId) {
        log.info("fromApp: Message={}, SessionId={}", message, sessionId);
//        dobiz( message, sessionId);
        fromAppMessageProcessor.process(message, sessionId);
    }

    @Override
    public void onCreate(SessionID sessionId) {
        log.info("onCreate: SessionId={}", sessionId);
    }

    @Override
    public void onLogon(SessionID sessionId) {
        log.info("onLogon: SessionId={}", sessionId);
    }

    @Override
    public void onLogout(SessionID sessionId) {
        log.info("onLogout: SessionId={}", sessionId);
    }

    @Override
    public void toAdmin(Message message, SessionID sessionId) {
        log.info("toAdmin: Message={}, SessionId={}", message, sessionId);
    }

    @Override
    public void toApp(Message message, SessionID sessionId) {
        log.info("toApp: Message={}, SessionId={}", message, sessionId);
    }

    @SneakyThrows
    private void dobiz(Message message, SessionID sessionId) {

        String msgType = message.getHeader().getString(MsgType.FIELD);

        switch (msgType) {
            case "D":
                newOrderSingle(message, sessionId);
                break;
            default:
                break;
        }
    }


    private static SessionID sessionID = new SessionID("FIX.4.4:NELOGICA->ZEROMARKETS");

    @SneakyThrows
    private void newOrderSingle(Message message, SessionID sessionId) {
        // create order

        // return ExecutionReport
        ExecutionReport executionReport = new ExecutionReport();

        executionReport.set(new ClOrdID(message.getString(ClOrdID.FIELD)));
        executionReport.set(new ExecID(String.valueOf(System.currentTimeMillis())));
        executionReport.set(new ExecType(ExecType.NEW));
        executionReport.set(new OrdStatus(OrdStatus.NEW));
        executionReport.set(new Symbol(message.getString(Symbol.FIELD)));
        executionReport.set(new Side(message.getChar(Side.FIELD)));
        executionReport.set(new LeavesQty(0));
        executionReport.set(new CumQty(1));
        executionReport.set(new AvgPx(100));

        if (Session.sendToTarget(executionReport, sessionID)) {
            log.info("发送成功:{}", executionReport);
        }

    }
}
