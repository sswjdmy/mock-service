package com.vitalgateway.grpcmock.grpc.mt5;

import com.ddm.grpc.mt5listener.request.ConfigurationServiceGrpc;
import com.ddm.grpc.mt5listener.request.RequestProto;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;


@Slf4j
@GrpcService
public class ConfigurationService extends ConfigurationServiceGrpc.ConfigurationServiceImplBase {
    @Override
    public void getServerTime(RequestProto.GetServerTimeReq request, StreamObserver<RequestProto.GetServerTimeResp> responseObserver) {
        long currentTimeMillis = System.currentTimeMillis();
        RequestProto.GetServerTimeResp response = RequestProto.GetServerTimeResp.newBuilder()
                .setNowSecond(currentTimeMillis/1000)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
