package com.vitalgateway.grpcmock.grpc.mt5;


import com.ddm.grpc.mt5listener.model.Mt5ListenerModel;
import com.ddm.grpc.mt5listener.request.RequestProto;
import com.ddm.grpc.mt5listener.request.TickServiceGrpc;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

@Slf4j
@GrpcService
public class TickService extends TickServiceGrpc.TickServiceImplBase {


    @Override
    public void getTickLast(RequestProto.GetTickLastReq request, StreamObserver<RequestProto.GetTickLastResp> responseObserver) {
        String symbol = request.getSymbol();

        RequestProto.GetTickLastResp getTickLastResp = RequestProto.GetTickLastResp.newBuilder().setTickShort(
                Mt5ListenerModel.MTTickShort.newBuilder()
                        .setSymbol(symbol)
                        .setBid(1.2345)
                        .setAsk(1.2350)
                        .setLast(1.2347)
                        .setDatetimeMsc(System.currentTimeMillis() / 1000)
                        .build()
        ).build();

        responseObserver.onNext(getTickLastResp);
        responseObserver.onCompleted();
    }
}
