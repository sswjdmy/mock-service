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
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


@Slf4j
@GrpcService
public class Mt5OrderPublisher extends OrderPublisherGrpc.OrderPublisherImplBase {
    @Getter
    private static final Map<Context, ServerCallStreamObserver<PublisherProto.PushOrderResp>> pushOrderMap = new ConcurrentHashMap<>();

    @Override
    public void pushOrder(PublisherProto.PushOrderReq request, StreamObserver<PublisherProto.PushOrderResp> responseObserver) {
        Context current = Context.current();
        pushOrderMap.put(current, (ServerCallStreamObserver<PublisherProto.PushOrderResp>) responseObserver);

        this.pushHeartbeat(responseObserver);

        for (int i = 0; i < 100; i++) {

            PublisherProto.PushOrderResp response = PublisherProto.PushOrderResp.newBuilder()
                    .setRetCode(CommonRetCode.SUCCESS)
                    .setOrder(Mt5ListenerModel.MTOrder.newBuilder()
                            .setState(Mt5ListenerEnums.OrderState.ORDER_STATE_FILLED)
                            .setOrder(31145368+i)
                            .setLogin(150000662)
                            .setSymbol("HK50")
                            .setReason(Mt5ListenerEnums.OrderReason.ORDER_REASON_DEALER)
                            .setDigits(2)
                            .setDigitsCurrency(2)
                            .setType(Mt5ListenerEnums.OrderType.ORDER_TYPE_BUY_LIMIT)
                            .setTypeFill(Mt5ListenerEnums.OrderFilling.ORDER_FILL_RETURN)
                            .setVolumeInitial(100000000)
                            .setVolumeCurrent(100000000)
                            .setPriceOrder(20000)
                            .setPriceCurrent(20735.24)
                            .setRateMargin(0.1287049323591228)
                            .setComment("BA-1"+i)
                            .setContractSize(1)
                            .build())
                    .build();
            responseObserver.onNext(response);
        }

        current.addListener(context -> {
            log.info("context cancelled: {}", context.isCancelled());
            pushOrderMap.remove(context);
        }, ExecutorUtil.DEFAULT_EXECUTOR);
    }

    private void pushHeartbeat(StreamObserver<PublisherProto.PushOrderResp> observer) {
        PublisherProto.PushOrderResp response = PublisherProto.PushOrderResp.newBuilder()
                .setServer("HEARTBEAT")
                .setOrder(Mt5ListenerModel.MTOrder.newBuilder().build())
                .build();
        CompletableFuture.runAsync(() -> {
            while (true) {
                try {
                    TimeUnit.SECONDS.sleep(25);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                if (((ServerCallStreamObserver<PublisherProto.PushOrderResp>) observer).isCancelled()) {
                    log.info("pushHeartbeat cancelled");
                    break;
                }
                log.info("pushHeartbeat");
                observer.onNext(response);
            }
        }, ExecutorUtil.DEFAULT_EXECUTOR);
    }
}
