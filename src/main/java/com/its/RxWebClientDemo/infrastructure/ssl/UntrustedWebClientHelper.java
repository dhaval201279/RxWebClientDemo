package com.its.RxWebClientDemo.infrastructure.ssl;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLException;

@Component
@Slf4j
//@Profile({"mock", "dev"})
public class UntrustedWebClientHelper implements WebClientSslHelper {
    @Override
    public SslContext getSslContext() {
        log.info("Entering getSslContext");
        SslContext sslContext = null;
        try {
            sslContext = SslContextBuilder
                            .forClient()
                            .trustManager(InsecureTrustManagerFactory.INSTANCE)
                            .build();
        } catch (SSLException e) {
            log.error("Error whilst initializing sslContext", e);
            e.printStackTrace();
        }
        log.info("Leaaving getSslContext after initializing sslContext");
        return sslContext;
    }
}
