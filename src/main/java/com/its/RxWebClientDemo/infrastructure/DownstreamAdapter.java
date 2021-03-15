package com.its.RxWebClientDemo.infrastructure;

import reactor.core.publisher.Mono;

public interface DownstreamAdapter {
    Mono<String> generateAlias(String cardNo);
}
