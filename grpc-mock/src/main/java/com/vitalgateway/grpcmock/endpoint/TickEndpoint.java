package com.vitalgateway.grpcmock.endpoint;


import ch.qos.logback.core.testUtil.RandomUtil;
import com.ddm.grpc.mt5listener.model.Mt5ListenerModel;
import com.ddm.grpc.mt5listener.publisher.PublisherProto;
import com.vitalgateway.grpcmock.grpc.mt5.Mt5TickPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

@Slf4j
@RestController
@RequestMapping("/api/tick")
@RequiredArgsConstructor
public class TickEndpoint {

    private final Mt5TickPublisher mt5TickPublisher;


    @GetMapping("/send")
    public String send(int groupCount, int groupSize) {

        List<PublisherProto.PushListTickResp> tickResps = new ArrayList<>();

        log.info("groupCount: {}, groupSize: {}", groupCount, groupSize);
        // create tick
        for (int i = 0; i < groupCount; i++) {
            PublisherProto.PushListTickResp.Builder builder = PublisherProto.PushListTickResp.newBuilder();

            long random = System.currentTimeMillis() % 100000;

            for (int j = 0; j < groupSize; j++) {
                int index = i * groupSize + j;
                builder.addTicks(Mt5ListenerModel.MTTickShort.newBuilder().setSymbol("HK50" + index)
                        .setBid(random + index)
                        .setAsk(random + index)
                        .setLast(random + index)
                        .setVolume(random + index)
                        .setDatetime(System.currentTimeMillis() / 1000)
                        .setDatetimeMsc(System.currentTimeMillis())
                        .build());
            }
            PublisherProto.PushListTickResp tickResp = builder.build();
            tickResps.add(tickResp);
        }

        log.info("create tick done");

        tickResps.forEach(mt5TickPublisher::onTick);

//        try (var exec = Executors.newVirtualThreadPerTaskExecutor();){
//            tickResps.forEach(t -> exec.submit(() -> {
//                mt5TickPublisher.onTick(t);
//            }));
//        }


        log.info("send tick done");
        // 多线程发送
        return "ok";
    }
}
