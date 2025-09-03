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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


@Slf4j
@GrpcService
public class Mt5TickPublisher extends TickPublisherGrpc.TickPublisherImplBase {
//    @Getter
//    private static final Map<Context, ServerCallStreamObserver<PublisherProto.PushTickResp>> pushTickMap = new ConcurrentHashMap<>();

    private static final List<StreamObserver<PublisherProto.PushListTickResp>> observers = new CopyOnWriteArrayList<>();


    @Getter
    private static final Map<Context, ServerCallStreamObserver<PublisherProto.PushListTickResp>> pushListTickMap = new ConcurrentHashMap<>();


    @Override
    public void pushListTick(PublisherProto.PushListTickReq request, StreamObserver<PublisherProto.PushListTickResp> responseObserver) {
        Context current = Context.current();
//        pushListTickMap.put(current, (ServerCallStreamObserver<PublisherProto.PushListTickResp>) responseObserver);
        observers.add(responseObserver);

        pushHeartbeat(responseObserver);

        current.addListener(context -> {
            log.info("context cancelled: {}", context.isCancelled());
            observers.remove(responseObserver);
//            pushListTickMap.remove(context);
        }, ExecutorUtil.DEFAULT_EXECUTOR);
    }




    public void onTickList(PublisherProto.PushListTickResp tickResp) {
        try (var exec = Executors.newVirtualThreadPerTaskExecutor()) {
            exec.execute(() -> safeOnNext(pollObserver(), tickResp));
        }
    }

    int index = 0;

    StreamObserver<PublisherProto.PushListTickResp> pollObserver() {
        if (observers.isEmpty()) {
            return null;
        }
        if (index >= observers.size()) {
            index = 0;
        }
        return observers.get(index++);
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
                safeOnNext(observer, response);
            }
        }, ExecutorUtil.DEFAULT_EXECUTOR);
    }

    private void safeOnNext(StreamObserver<PublisherProto.PushListTickResp> observer, PublisherProto.PushListTickResp response) {
        synchronized (observer) {
            observer.onNext(response);
        }
    }
}
