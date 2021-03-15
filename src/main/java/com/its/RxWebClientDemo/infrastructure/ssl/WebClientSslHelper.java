package com.its.RxWebClientDemo.infrastructure.ssl;

import io.netty.handler.ssl.SslContext;

public interface WebClientSslHelper {
    SslContext getSslContext();
}
