package com.vitalgateway.grpcmock.grpc.st;

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
import net.devh.boot.grpc.server.service.GrpcService;

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
@GrpcService
public class StOrderPublisher extends OrderServiceGrpc.OrderServiceImplBase {

    @Getter
    private static final Map<Context, ServerCallStreamObserver<SecurityTradePlatformProto.PushOrderResp>> pushOrderMap = new ConcurrentHashMap<>();

    private static final Map<StreamObserver<SecurityTradePlatformProto.PushOrderResp>, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    private static final List<StreamObserver<PublisherProto.PushListTickResp>> observers = new CopyOnWriteArrayList<>();


    @Override
    public void pushOrder(SecurityTradePlatformProto.PushOrderReq request, StreamObserver<SecurityTradePlatformProto.PushOrderResp> responseObserver) {
        long threadId = Thread.currentThread().getId();


        log.info("pushOrder connected, threadId: {}", threadId);

        Context current = Context.current();
        pushOrderMap.put(current, (ServerCallStreamObserver<SecurityTradePlatformProto.PushOrderResp>) responseObserver);

        pushHeartbeat(responseObserver);
        pushOrder(responseObserver, 1);

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
                                    .setOrder(String.valueOf(System.currentTimeMillis() + i))
                                    .setAccountId("150000662")
                                    .setSymbol("HK50")
                                    .setState(SecurityTradePlatformEnums.OrderState.forNumber(i % 14 ))
                                    .setType(SecurityTradePlatformEnums.OrderType.ORDER_TYPE_LIMIT)
                                    .setSide(SecurityTradePlatformEnums.OrderSide.ORDER_SIDE_BUY)
                                    .setTif(SecurityTradePlatformEnums.TimeInForce.ORDER_TIF_IOC)
                                    .setOrderQty(100000000)
                                    .setCumQty(100000000)
                                    .setLeavesQty(20000)
                                    .setLastQty(20735.24)
                                    .setOrderPrice(0.1287049323591228)
                                    .setStopPrice(0.1287049323591228)
                                    .setLastPrice(0.1287049323591228)
                                    .setAvgPrice(0.1287049323591228)
                                    .setPosition("")
                                    .setCreateTime(System.currentTimeMillis())
                                    .setExpirationTime(0)
                                    .setUpdateTime(System.currentTimeMillis())
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


    @Override
    public void getOrder(SecurityTradePlatformProto.GetOrderReq request, StreamObserver<SecurityTradePlatformProto.GetOrderResp> responseObserver) {
        String order = request.getOrder();

        // todo
        SecurityTradePlatformProto.GetOrderResp eurusd = SecurityTradePlatformProto.GetOrderResp.newBuilder().setData(
                SecurityTradePlatformModel.OrderWithDeal.newBuilder()
                        .setOrder(order)
                        .setSymbol("EURUSD")
                        .build()
        ).build();

        responseObserver.onNext(eurusd);
        responseObserver.onCompleted();
    }
}
