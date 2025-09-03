package com.vitalgateway.fixserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import quickfix.Acceptor;
import quickfix.Application;
import quickfix.ConfigError;
import quickfix.FileLogFactory;
import quickfix.LogFactory;
import quickfix.MessageFactory;
import quickfix.MessageStoreFactory;
import quickfix.SessionSettings;
import quickfix.ThreadedSocketAcceptor;


@SpringBootApplication
public class FixServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(FixServerApplication.class, args);
    }


    @Bean
    public Acceptor serverAcceptor(Application serverApplication,
                                   MessageStoreFactory serverMessageStoreFactory,
                                   SessionSettings serverSessionSettings,
                                   LogFactory serverLogFactory,
                                   MessageFactory serverMessageFactory) throws ConfigError {

        return new ThreadedSocketAcceptor(serverApplication, serverMessageStoreFactory, serverSessionSettings,
                serverLogFactory, serverMessageFactory);
    }

    @Bean
    public LogFactory serverLogFactory(SessionSettings serverSessionSettings) {
        return new FileLogFactory(serverSessionSettings);
    }

}
