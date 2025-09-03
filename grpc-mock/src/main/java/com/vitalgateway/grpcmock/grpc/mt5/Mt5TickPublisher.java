package com.vitalgateway.grpcmock.grpc.mt5;

import com.ddm.grpc.mt5listener.model.Mt5ListenerModel;
import com.ddm.grpc.mt5listener.publisher.PublisherProto;
import com.ddm.grpc.mt5listener.publisher.TickPublisherGrpc;
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
public class Mt5TickPublisher extends TickPublisherGrpc.TickPublisherImplBase {
    @Getter
    private static final Map<Context, ServerCallStreamObserver<PublisherProto.PushTickResp>> pushTickMap = new ConcurrentHashMap<>();

    @Override
    public void pushTick(PublisherProto.PushTickReq request, StreamObserver<PublisherProto.PushTickResp> responseObserver) {
        Context current = Context.current();
        pushTickMap.put(current, (ServerCallStreamObserver<PublisherProto.PushTickResp>) responseObserver);

        current.addListener(context -> {
            log.info("context cancelled: {}", context.isCancelled());
            pushTickMap.remove(context);
        }, ExecutorUtil.DEFAULT_EXECUTOR);
    }

    @Getter
    private static final Map<Context, ServerCallStreamObserver<PublisherProto.PushListTickResp>> pushListTickMap = new ConcurrentHashMap<>();


    @Override
    public void pushListTick(PublisherProto.PushListTickReq request, StreamObserver<PublisherProto.PushListTickResp> responseObserver) {
        Context current = Context.current();
        pushListTickMap.put(current, (ServerCallStreamObserver<PublisherProto.PushListTickResp>) responseObserver);

        pushHeartbeat(responseObserver);

        current.addListener(context -> {
            log.info("context cancelled: {}", context.isCancelled());
            pushListTickMap.remove(context);
        }, ExecutorUtil.DEFAULT_EXECUTOR);
    }

    public void onTick(PublisherProto.PushListTickResp tickResp) {
        for (ServerCallStreamObserver<PublisherProto.PushListTickResp> observer : pushListTickMap.values()) {
            if (observer.isCancelled()) {
                continue;
            }
            log.info("onTick: {}", tickResp.getTicksCount());
            observer.onNext(tickResp);
        }
    }



    private void pushHeartbeat(StreamObserver<PublisherProto.PushListTickResp> observer) {
        PublisherProto.PushListTickResp response = PublisherProto.PushListTickResp.newBuilder()
                .addTicks(Mt5ListenerModel.MTTickShort.newBuilder()
                        .setSymbol("HEARTBEAT")
                        .build())
                .build();
        CompletableFuture.runAsync(() -> {
            while (true) {
                try {
                    TimeUnit.SECONDS.sleep(25);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                if (((ServerCallStreamObserver<PublisherProto.PushListTickResp>) observer).isCancelled()) {
                    log.info("pushHeartbeat cancelled");
                    break;
                }
                log.info("pushHeartbeat");
                observer.onNext(response);
            }
        }, ExecutorUtil.DEFAULT_EXECUTOR);
    }
}
