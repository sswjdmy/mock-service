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
import quickfix.field.SubscriptionRequestType;
import quickfix.field.Symbol;
import quickfix.fix50sp2.MarketDataRequest;

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
}
