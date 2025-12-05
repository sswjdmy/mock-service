package com.vitalgateway.fixserver.endpoint;


import com.vitalgateway.fixserver.fix.field.Memo;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.field.Account;
import quickfix.field.ClOrdID;
import quickfix.field.ExpireDate;
import quickfix.field.OrdType;
import quickfix.field.OrderID;
import quickfix.field.OrderQty;
import quickfix.field.OrigClOrdID;
import quickfix.field.Price;
import quickfix.field.SecurityExchange;
import quickfix.field.Side;
import quickfix.field.StopPx;
import quickfix.field.Symbol;
import quickfix.field.TimeInForce;
import quickfix.field.TransactTime;
import quickfix.fix44.NewOrderSingle;
import quickfix.fix44.OrderCancelReplaceRequest;
import quickfix.fix44.OrderCancelRequest;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrderEndpoint {

    private static final String SessionId = "FIX.4.4:NELOGICA->ZEROMARKETS";


    /**
     * 批量创建价格和数量都为1的limit订单，尽量少冻结金额,避免余额不足
     * @param account 账户id
     * @param number 订单数量
     */
    @SneakyThrows
    @GetMapping("/create-batch")
    public void createOrderBatch(String account, int number) {

        long start = System.currentTimeMillis();

        SessionID sessionID = new SessionID(SessionId);
        String prefix = UUID.randomUUID().toString();
        for (int i = 1; i <= number; i++) {
            NewOrderSingle request = new NewOrderSingle();
            request.setString(OrdType.FIELD, "2"); // 限价单
            request.setString(Side.FIELD, "1");
            request.setString(TimeInForce.FIELD, "0");
            request.setString(ExpireDate.FIELD, "20251118");
            request.setString(ClOrdID.FIELD, prefix + i);
            request.setString(OrigClOrdID.FIELD, "orig" + prefix + i);
            request.setString(Account.FIELD, account);
            request.setString(TransactTime.FIELD, "20251118-04:22:28.109");
            request.setString(Price.FIELD, "1"); // 价格
            request.setString(Memo.FIELD, "");
            request.setString(OrderQty.FIELD, "1");
            request.setString(Symbol.FIELD, "BABA");
            request.setString(SecurityExchange.FIELD, "BA");
            Session.sendToTarget(request, sessionID);
            log.info("创建订单: clOrdID={}", prefix + i);
        }
        long end = System.currentTimeMillis();
        log.info("创建订单完成,数量:{},耗时:{}ms",number,(end - start));
    }



    /**
     * 创建订单
     * {@snippet lang = "JSON":
     * {
     *   "ordType": "1",
     *   "side": "1",
     *   "timeInForce": "0",
     *   "expireDate": "20251118",
     *   "clOrdID": "clord11152",
     *   "origClOrdID": "orig2222",
     *   "account": "3",
     *   "transactTime": "20251118-04:22:28.109",
     *   "price": "1",
     *   "stopPx": null,
     *   "memo": "",
     *   "orderQty": "1",
     *   "symbol": "BABA",
     *   "securityExchange": "BA"
     * }
     *}
     *
     * @param orderRequest
     * @return
     */
    @SneakyThrows
    @PostMapping("/create")
    public String createOrder(@RequestBody CreateOrderRequest orderRequest) {

        SessionID sessionID = new SessionID(SessionId);
        NewOrderSingle request = new NewOrderSingle();
        request.setString(OrdType.FIELD, orderRequest.getOrdType());
        request.setString(Side.FIELD, orderRequest.getSide());
        request.setString(TimeInForce.FIELD, orderRequest.getTimeInForce());
        request.setString(ExpireDate.FIELD, orderRequest.getExpireDate());
        request.setString(ClOrdID.FIELD, orderRequest.getClOrdID());
        request.setString(OrigClOrdID.FIELD, orderRequest.getOrigClOrdID());
        request.setString(Account.FIELD, orderRequest.getAccount());
        request.setString(TransactTime.FIELD, orderRequest.getTransactTime());
        Optional.ofNullable(orderRequest.getPrice()).ifPresent(e -> request.setString(Price.FIELD, e));
        Optional.ofNullable(orderRequest.getStopPx()).ifPresent(e -> request.setString(StopPx.FIELD, e));
        request.setString(Memo.FIELD, orderRequest.getMemo());
        request.setString(OrderQty.FIELD, orderRequest.getOrderQty());
        request.setString(Symbol.FIELD, orderRequest.getSymbol());
        request.setString(SecurityExchange.FIELD, orderRequest.getSecurityExchange());
        Session.sendToTarget(request, sessionID);
        return "ok";
    }

    /**
     * {@snippet lang = "JSON":
     * {
     *   "ordType": "2",
     *   "side": "1",
     *   "timeInForce": "0",
     *   "expireDate": "20251118",
     *   "clOrdID": "RRclord11155",
     *   "origClOrdID": "orig2222",
     *   "orderID": "Rclord11155",
     *   "account": "3",
     *   "transactTime": "20251118-04:22:28.109",
     *   "price": "2",
     *   "stopPx": null,
     *   "memo": "",
     *   "orderQty": "1",
     *   "symbol": "BABA",
     *   "securityExchange": "BA"
     * }
     *}
     *
     * @param orderRequest
     * @return
     */
    @SneakyThrows
    @PostMapping("/update")
    public String updateOrder(@RequestBody UpdateOrderRequest orderRequest) {

        SessionID sessionID = new SessionID(SessionId);
        OrderCancelReplaceRequest request = new OrderCancelReplaceRequest();
        request.setString(OrdType.FIELD, orderRequest.getOrdType());
        request.setString(Side.FIELD, orderRequest.getSide());
        request.setString(TimeInForce.FIELD, orderRequest.getTimeInForce());
        request.setString(ExpireDate.FIELD, orderRequest.getExpireDate());
        request.setString(ClOrdID.FIELD, orderRequest.getClOrdID());
        request.setString(OrigClOrdID.FIELD, orderRequest.getOrigClOrdID());
        request.setString(OrderID.FIELD, orderRequest.getOrderID());
        request.setString(Account.FIELD, orderRequest.getAccount());
        request.setString(TransactTime.FIELD, orderRequest.getTransactTime());
        Optional.ofNullable(orderRequest.getPrice()).ifPresent(e -> request.setString(Price.FIELD, e));
        Optional.ofNullable(orderRequest.getStopPx()).ifPresent(e -> request.setString(StopPx.FIELD, e));
        request.setString(Memo.FIELD, orderRequest.getMemo());
        request.setString(OrderQty.FIELD, orderRequest.getOrderQty());
        request.setString(Symbol.FIELD, orderRequest.getSymbol());
        request.setString(SecurityExchange.FIELD, orderRequest.getSecurityExchange());
        Session.sendToTarget(request, sessionID);
        return "ok";
    }


    /**
     * {@snippet lang = "JSON":
     * {
     *   "clOrdID": "clord11144c",
     *   "origClOrdID": "clord11144",
     *   "account": "3",
     *   "orderID": "RRclord11155",
     *   "transactTime": "20251104-04:22:28.109",
     *   "side": "1",
     *   "orderQty": "1",
     *   "symbol": "BABA",
     *   "securityExchange": "BA"
     * }
     *}
     *
     * @param cancelOrderRequest
     * @return
     */
    @SneakyThrows
    @PostMapping("/cancel")
    public String cancelOrder(@RequestBody CancelOrderRequest cancelOrderRequest) {
        SessionID sessionID = new SessionID(SessionId);
        OrderCancelRequest request = new OrderCancelRequest();
        request.setString(ClOrdID.FIELD, cancelOrderRequest.getClOrdID());
        request.setString(OrigClOrdID.FIELD, cancelOrderRequest.getOrigClOrdID());
        request.setString(Account.FIELD, cancelOrderRequest.getAccount());
        request.setString(OrderID.FIELD, cancelOrderRequest.getOrderID());
        request.setString(TransactTime.FIELD, cancelOrderRequest.getTransactTime());
        request.setString(Side.FIELD, cancelOrderRequest.getSide());
        request.setString(OrderQty.FIELD, cancelOrderRequest.getOrderQty());
        request.setString(Symbol.FIELD, cancelOrderRequest.getSymbol());
        request.setString(SecurityExchange.FIELD, cancelOrderRequest.getSecurityExchange());
        Session.sendToTarget(request, sessionID);
        return "ok";
    }


    @PostMapping("/cancel-all")
    public String cancelAllOrders(String account) {
        // TODO 批量取消订单
        return "ok";

    }


    @Data
    public static class CreateOrderRequest {
        private String ordType;
        private String side;
        private String timeInForce;
        private String expireDate;
        private String clOrdID;
        private String origClOrdID;
        private String account;
        private String transactTime;
        private String price;
        private String stopPx;
        private String memo;
        private String orderQty;
        private String symbol;
        private String securityExchange;

    }

    @Data
    public static class UpdateOrderRequest {
        private String ordType;
        private String side;
        private String timeInForce;
        private String expireDate;
        private String clOrdID;
        private String origClOrdID;
        private String orderID;
        private String account;
        private String transactTime;
        private String price;
        private String stopPx;
        private String memo;
        private String orderQty;
        private String symbol;
        private String securityExchange;
    }


    @Data
    public static class CancelOrderRequest {
        private String clOrdID;
        private String origClOrdID;
        private String account;
        private String orderID;
        private String transactTime;
        private String side;
        private String orderQty;
        private String symbol;
        private String securityExchange;
    }
}
