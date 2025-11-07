package com.vitalgateway.fixserver.endpoint;


import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.field.MDEntryType;
import quickfix.field.MDReqID;
import quickfix.field.MDUpdateType;
import quickfix.field.MarketDepth;
import quickfix.field.NoMDEntryTypes;
import quickfix.field.NoRelatedSym;
import quickfix.field.SecurityID;
import quickfix.field.SecurityListRequestType;
import quickfix.field.SecurityReqID;
import quickfix.field.SecurityStatusReqID;
import quickfix.field.SubscriptionRequestType;
import quickfix.field.Symbol;
import quickfix.field.TradSesMethod;
import quickfix.field.TradSesReqID;
import quickfix.fix50sp2.MarketDataRequest;
import quickfix.fix50sp2.SecurityListRequest;
import quickfix.fix50sp2.SecurityStatusRequest;
import quickfix.fix50sp2.TradingSessionStatusRequest;

@Slf4j
@RestController
@RequestMapping("/marketdata")
public class MarketDataEndpoint {

    private static final String SessionId = "FIXT.1.1:NELOGICA->ZEROMARKETS";

    /**
     * 模拟7000个Symbol的快照和订阅
     *
     * @return
     */
    @SneakyThrows
    @GetMapping("/subscribe")
    public String subscribe() {
        SessionID sessionID = new SessionID(SessionId);
        SubscriptionRequestType subscriptionRequestType = new SubscriptionRequestType(SubscriptionRequestType.SNAPSHOT_UPDATES);
        MarketDepth marketDepth = new MarketDepth(0);
        MDUpdateType mdUpdateType = new MDUpdateType(1);

        MarketDataRequest.NoMDEntryTypes group1 = new MarketDataRequest.NoMDEntryTypes();
        group1.setField(new MDEntryType(MDEntryType.BID));
        MarketDataRequest.NoMDEntryTypes group2 = new MarketDataRequest.NoMDEntryTypes();
        group2.setField(new MDEntryType(MDEntryType.OFFER));

        for (int i = 1; i <= 7000; i++) {
            String symbol = "Symbol-" + i;
            MarketDataRequest mdr = new MarketDataRequest();
            mdr.setField(new MDReqID("req-" + i));
            mdr.setField(subscriptionRequestType);
            mdr.setField(marketDepth);
            mdr.setField(mdUpdateType);
            mdr.setField(new NoMDEntryTypes(2));
            mdr.addGroup(group1);
            mdr.addGroup(group2);
            mdr.setField(new NoRelatedSym(1));
            MarketDataRequest.NoRelatedSym group3 = new MarketDataRequest.NoRelatedSym();
            group3.setField(new Symbol(symbol));
            group3.setField(new SecurityID(symbol));
            mdr.addGroup(group3);
            Session.sendToTarget(mdr, sessionID);
        }
        return "ok";
    }

    @SneakyThrows
    @GetMapping("/tradeSession")
    public String tradeSession() {
        SessionID sessionID = new SessionID(SessionId);
        TradingSessionStatusRequest request = new TradingSessionStatusRequest();
        request.setString(TradSesReqID.FIELD, "trade-session-1");
        request.setChar(SubscriptionRequestType.FIELD, '0');
        Session.sendToTarget(request, sessionID);
        return "ok";
    }


    @SneakyThrows
    @GetMapping("/securityStatus")
    public String securityStatus() {
        SessionID sessionID = new SessionID(SessionId);
        SecurityStatusRequest request = new SecurityStatusRequest();
        request.setString(SecurityStatusReqID.FIELD, "123");
        request.setString(SecurityID.FIELD, "AAPL");
        request.setString(Symbol.FIELD, "AAPL");
        request.setChar(SubscriptionRequestType.FIELD, '1');
        Session.sendToTarget(request, sessionID);
        return "ok";
    }

    @SneakyThrows
    @GetMapping("/securityList")
    public String securityList() {
        SessionID sessionID = new SessionID(SessionId);
        SecurityListRequest request = new SecurityListRequest();
        request.setString(SecurityReqID.FIELD, "123");
        request.setInt(SecurityListRequestType.FIELD, 4);
        Session.sendToTarget(request, sessionID);
        return "ok";
    }

}
