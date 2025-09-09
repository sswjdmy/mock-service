package com.vitalgateway.grpcmock.grpc.mt5;

import com.ddm.grpc.common.consts.CommonRetCode;
import com.ddm.grpc.mt5listener.enums.Mt5ListenerEnums;
import com.ddm.grpc.mt5listener.model.Mt5ListenerModel;
import com.ddm.grpc.mt5listener.publisher.OrderPublisherGrpc;
import com.ddm.grpc.mt5listener.publisher.PublisherProto;
import com.vitalgateway.common.utils.concurrent.ExecutorUtil;
import io.grpc.Context;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import jakarta.annotation.PostConstruct;
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
public class Mt5OrderPublisher extends OrderPublisherGrpc.OrderPublisherImplBase {
    @Getter
    private static final Map<Context, ServerCallStreamObserver<PublisherProto.PushOrderResp>> pushOrderMap = new ConcurrentHashMap<>();

    private static final Map<StreamObserver<PublisherProto.PushOrderResp>, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    private static final List<StreamObserver<PublisherProto.PushListTickResp>> observers = new CopyOnWriteArrayList<>();


    @Override
    public void pushOrder(PublisherProto.PushOrderReq request, StreamObserver<PublisherProto.PushOrderResp> responseObserver) {
        Context current = Context.current();
        pushOrderMap.put(current, (ServerCallStreamObserver<PublisherProto.PushOrderResp>) responseObserver);

        pushHeartbeat(responseObserver);
        pushOrder(responseObserver, 500);

        current.addListener(context -> {
            log.info("context cancelled: {}", context.isCancelled());
            pushOrderMap.remove(context);
        }, ExecutorUtil.DEFAULT_EXECUTOR);
    }


    private void pushOrder(StreamObserver<PublisherProto.PushOrderResp> observer, int count) {
        CompletableFuture.runAsync(() -> {
            while (true) {
                if (((ServerCallStreamObserver<PublisherProto.PushOrderResp>) observer).isCancelled()) {
                    log.info("pushOrder cancelled");
                    break;
                }
                for (int i = 0; i < count; i++) {
                    PublisherProto.PushOrderResp response = PublisherProto.PushOrderResp.newBuilder()
                            .setRetCode(CommonRetCode.SUCCESS)
                            .setOrder(Mt5ListenerModel.MTOrder.newBuilder()
                                    .setState(Mt5ListenerEnums.OrderState.ORDER_STATE_FILLED)
                                    .setOrder(31145368 + i)
                                    .setLogin(150000662)
                                    .setSymbol("HK50")
                                    .setReason(Mt5ListenerEnums.OrderReason.ORDER_REASON_DEALER)
                                    .setDigits(2)
                                    .setDigitsCurrency(2)
                                    .setType(Mt5ListenerEnums.OrderType.ORDER_TYPE_BUY_LIMIT)
                                    .setTypeFill(Mt5ListenerEnums.OrderFilling.ORDER_FILL_RETURN)
                                    .setTypeTime(Mt5ListenerEnums.OrderTime.ORDER_TIME_GTC)
                                    .setReason(Mt5ListenerEnums.OrderReason.ORDER_REASON_CLIENT)
                                    .setVolumeInitial(100000000)
                                    .setVolumeCurrent(100000000)
                                    .setPriceOrder(20000)
                                    .setPriceCurrent(20735.24)
                                    .setRateMargin(0.1287049323591228)
                                    .setComment("BA-1" + i)
                                    .setContractSize(1)
                                    .setTimeSetupMsc(System.currentTimeMillis())
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


    private void pushHeartbeat(StreamObserver<PublisherProto.PushOrderResp> observer) {
        PublisherProto.PushOrderResp response = PublisherProto.PushOrderResp.newBuilder()
                .setOrder(Mt5ListenerModel.MTOrder.newBuilder().setSymbol("HEARTBEAT").build())
                .build();

        CompletableFuture.runAsync(() -> {
            while (true) {
                if (((ServerCallStreamObserver<PublisherProto.PushOrderResp>) observer).isCancelled()) {
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

    private void safeOnNext(StreamObserver<PublisherProto.PushOrderResp> observer, PublisherProto.PushOrderResp response) {
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
