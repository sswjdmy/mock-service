package com.vitalgateway.grpcmock.grpc.st;

import com.ddm.grpc.common.consts.CommonRetCode;
import com.ddm.grpc.mt5listener.enums.Mt5ListenerEnums;
import com.ddm.grpc.mt5listener.model.Mt5ListenerModel;
import com.ddm.grpc.mt5listener.publisher.PublisherProto;
import com.ddm.grpc.securityTradePlatform.enums.SecurityTradePlatformEnums;
import com.ddm.grpc.securityTradePlatform.model.SecurityTradePlatformModel;
import com.ddm.grpc.securityTradePlatform.service.OrderServiceGrpc;
import com.ddm.grpc.securityTradePlatform.service.SecurityTradePlatformProto;
import com.vitalgateway.common.utils.concurrent.ExecutorUtil;
import io.grpc.Context;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;



@Slf4j
@Service
public class StOrderPublisher extends OrderServiceGrpc.OrderServiceImplBase {

    @Getter
    private static final Map<Context, ServerCallStreamObserver<SecurityTradePlatformProto.PushOrderResp>> pushOrderMap = new ConcurrentHashMap<>();

    private static final Map<StreamObserver<SecurityTradePlatformProto.PushOrderResp>, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    private static final List<StreamObserver<PublisherProto.PushListTickResp>> observers = new CopyOnWriteArrayList<>();


    @Override
    public void pushOrder(SecurityTradePlatformProto.PushOrderReq request, StreamObserver<SecurityTradePlatformProto.PushOrderResp> responseObserver) {
        Context current = Context.current();
        pushOrderMap.put(current, (ServerCallStreamObserver<SecurityTradePlatformProto.PushOrderResp>) responseObserver);

        pushHeartbeat(responseObserver);
        pushOrder(responseObserver, 500);

        current.addListener(context -> {
            log.info("context cancelled: {}", context.isCancelled());
            pushOrderMap.remove(context);
        }, ExecutorUtil.DEFAULT_EXECUTOR);
    }


    private void pushOrder(StreamObserver<SecurityTradePlatformProto.PushOrderResp> observer, int count) {
        CompletableFuture.runAsync(() -> {
            while (true) {
                if (((ServerCallStreamObserver<SecurityTradePlatformProto.PushOrderResp>) observer).isCancelled()) {
                    log.info("pushOrder cancelled");
                    break;
                }
                for (int i = 0; i < count; i++) {
                    SecurityTradePlatformProto.PushOrderResp response = SecurityTradePlatformProto.PushOrderResp.newBuilder()
                            .setData(SecurityTradePlatformModel.OrderWithDeal.newBuilder()
                                    .setOrder("31145368" + i)
                                    .setAccount(150000662)
                                    .setSymbol("HK50")
                                    .setState(SecurityTradePlatformEnums.OrderState.ORDER_STATE_FILLED)
                                    .setReason(SecurityTradePlatformEnums.OrderReason.ORDER_REASON_DEALER)
                                    .setType(SecurityTradePlatformEnums.OrderType.ORDER_TYPE_BUY_LIMIT)
                                    .setTypeFill(SecurityTradePlatformEnums.FillType.ORDER_FILL_RETURN)
                                    .setTypeTime(SecurityTradePlatformEnums.TimeType.ORDER_TIME_GTC)
                                    .setReason(SecurityTradePlatformEnums.OrderReason.ORDER_REASON_CLIENT)
                                    .setOrderQty(100000000)
                                    .setCumQty(100000000)
                                    .setLeavesQty(20000)
                                    .setLastQty(20735.24)
                                    .setOrderPrice(0.1287049323591228)
                                    .setStopPx(0.1287049323591228)
                                    .setLastPx(0.1287049323591228)
                                    .setAvgPx(0.1287049323591228)
                                    .setPositionId("")
                                    .setTimeSetup(System.currentTimeMillis())
                                    .setTimeExpiration(0)
                                    .setTimeDone(System.currentTimeMillis())
                                    .build())
                            .build();
                    safeOnNext(observer, response);
                }
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            }
        }, Executors.newVirtualThreadPerTaskExecutor());
        log.info("pushOrder started");
    }


    private void pushHeartbeat(StreamObserver<SecurityTradePlatformProto.PushOrderResp> observer) {
        SecurityTradePlatformProto.PushOrderResp response = SecurityTradePlatformProto.PushOrderResp.newBuilder()
                .setData(SecurityTradePlatformModel.OrderWithDeal.newBuilder().setOrder("HEARTBEAT").build())
                .build();

        CompletableFuture.runAsync(() -> {
            while (true) {
                if (((ServerCallStreamObserver<SecurityTradePlatformProto.PushOrderResp>) observer).isCancelled()) {
                    log.info("pushHeartbeat cancelled");
                    break;
                }
                log.info("pushHeartbeat");
                safeOnNext(observer, response);

                try {
                    TimeUnit.SECONDS.sleep(30);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }, Executors.newVirtualThreadPerTaskExecutor());

        log.info("pushHeartbeat started");
    }

    private void safeOnNext(StreamObserver<SecurityTradePlatformProto.PushOrderResp> observer, SecurityTradePlatformProto.PushOrderResp response) {
        ReentrantLock lock = lockMap.computeIfAbsent(observer, k -> new ReentrantLock());
        try {
            lock.lock();
            observer.onNext(response);
        } finally {
            lock.unlock();
        }

    }

    AtomicInteger index = new AtomicInteger(0);

    StreamObserver<PublisherProto.PushListTickResp> pollObserver() {
        if (observers.isEmpty()) {
            return null;
        }
        if (index.get() >= observers.size()) {
            index.set(0);
        }
        return observers.get(index.getAndIncrement());
    }


}
