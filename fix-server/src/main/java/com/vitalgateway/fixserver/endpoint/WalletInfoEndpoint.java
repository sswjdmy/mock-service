package com.vitalgateway.fixserver.endpoint;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.field.Account;
import quickfix.field.SubscriptionRequestType;
import quickfix.fix44.Message;

@Slf4j
@RestController
@RequestMapping("/wallet")
public class WalletInfoEndpoint {

    private static final String SessionId = "FIX.4.4:NELOGICA->ZEROMARKETS";


    @SneakyThrows
    @GetMapping("/request")
    public String request(String account) {

        SessionID sessionID = new SessionID(SessionId);

        Message message = new Message();
        message.getHeader().setField(new quickfix.field.MsgType("BK"));
        message.setString(Account.FIELD,account);
        message.setChar(SubscriptionRequestType.FIELD,'1');

        Session.sendToTarget(message, sessionID);
        return "ok";
    }

}
