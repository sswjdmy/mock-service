package com.vitalgateway.grpcmock.grpc.st;


import com.ddm.grpc.securityTradePlatform.enums.SecurityTradePlatformEnums;
import com.ddm.grpc.securityTradePlatform.model.SecurityTradePlatformModel;
import com.ddm.grpc.securityTradePlatform.service.AccountServiceGrpc;
import com.ddm.grpc.securityTradePlatform.service.SecurityTradePlatformProto;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

@Slf4j
@GrpcService
public class AccountService extends AccountServiceGrpc.AccountServiceImplBase {


    @Override
    public void createOrderForAccount(SecurityTradePlatformProto.CreateOrderReq request, StreamObserver<SecurityTradePlatformProto.CreateOrderResp> responseObserver) {
        log.info("receive createOrderForAccount request: {}", request);
        super.createOrderForAccount(request, responseObserver);
    }

    @Override
    public void listAccount(SecurityTradePlatformProto.ListAccountReq request, StreamObserver<SecurityTradePlatformProto.ListAccountResp> responseObserver) {
        log.info("receive listAccount request: {}", request);

        SecurityTradePlatformProto.ListAccountResp.Builder builder = SecurityTradePlatformProto.ListAccountResp.newBuilder();
        for (Long l : request.getAccountsList()) {
            SecurityTradePlatformModel.Account account = SecurityTradePlatformModel.Account.newBuilder()
                    .setAccount(l)
                    .setBalance(100000 + l)
                    .setCredit(100)
                    .setCurrency(SecurityTradePlatformEnums.CurrencyType.USD)
                    .setMargin(10000)
                    .setEquity(90000 + l)
                    .setMarginFree(l)
                    .build();
            builder.addData(account);
        }

        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }
}
