package com.vitalgateway.grpcmock.endpoint;


import com.ddm.grpc.mt5listener.model.Mt5ListenerModel;
import com.ddm.grpc.mt5listener.publisher.PublisherProto;
import com.google.common.collect.Lists;
import com.vitalgateway.grpcmock.grpc.mt5.Mt5TickPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/tick")
@RequiredArgsConstructor
public class TickEndpoint {

    private final Mt5TickPublisher mt5TickPublisher;


    @GetMapping("/send")
    public String send(int perSymbolCount) {

        List<PublisherProto.PushListTickResp> tickResps = new ArrayList<>();

        log.info("perSymbolCount: {}", perSymbolCount);

        List<Mt5ListenerModel.MTTickShort> ticks = new ArrayList<>();
        // create tick
        for (int i = 0; i < perSymbolCount; i++) {
            for (int j = 0; j < 7000; j++) {
                long random = System.currentTimeMillis() % 100000;
                int index = i * 7000 + j;
                Mt5ListenerModel.MTTickShort tickShort = Mt5ListenerModel.MTTickShort.newBuilder().setSymbol("Symbol-" + j)
                        .setBid(random + index)
                        .setAsk(random + index)
                        .setLast(random + index)
                        .setVolume(random + index)
                        .setDatetime(System.currentTimeMillis() / 1000)
                        .setDatetimeMsc(System.currentTimeMillis())
                        .build();
                ticks.add(tickShort);
            }
        }

        List<List<Mt5ListenerModel.MTTickShort>> partitioned = Lists.partition(ticks,10);


        List<PublisherProto.PushListTickResp> listTickResps = partitioned.stream().map(list -> {
            PublisherProto.PushListTickResp.Builder builder = PublisherProto.PushListTickResp.newBuilder();
            builder.addAllTicks(list);
            return builder.build();
        }).toList();
        log.info("listTickResps created. size: {}", listTickResps.size());

        listTickResps.forEach(mt5TickPublisher::onTickList);

        return "ok";
    }
}
