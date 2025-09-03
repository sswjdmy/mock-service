package com.vitalgateway.grpcmock.grpc.mt5;

import com.ddm.grpc.mt5listener.request.RequestProto;
import com.ddm.grpc.mt5listener.request.SymbolServiceGrpc;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

@Slf4j
@GrpcService
public class SymbolService extends SymbolServiceGrpc.SymbolServiceImplBase {

    @Override
    public void getSymbol(RequestProto.GetSymbolReq request, StreamObserver<RequestProto.GetSymbolResp> responseObserver) {
        super.getSymbol(request, responseObserver);
    }

    @Override
    public void listSymbolByCategory(RequestProto.ListSymbolByCategoryReq request, StreamObserver<RequestProto.ListSymbolByCategoryResp> responseObserver) {
        responseObserver.onNext(RequestProto.ListSymbolByCategoryResp.newBuilder().build());
        responseObserver.onCompleted();
    }
}
