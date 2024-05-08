package com.its.RxWebClientDemo.infrastructure;

import com.its.RxWebClientDemo.infrastructure.ssl.UntrustedWebClientHelper;
import com.its.RxWebClientDemo.infrastructure.ssl.WebClientSslHelper;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.*;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.resources.LoopResources;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
@Slf4j
@Getter
@Setter
public class WebClientConfig {

    @Value("${webclient.http.max.connections:50}")
    private Integer maxConnections;

    @Value("${webclient.http.acquire.timeout.millisec:5000}")
    private Integer acquireTimeoutMillis;

    @Value("${webclient.http.connection.timeout.millisec:20000}")
    private Integer connectionTimeoutMillis;

    @Value("${webclient.http.max.retry.attempts:5}")
    private Integer maxRetryAttempts;

    @Value("${webclient.http.retry.first.backoff.seconds:50}")
    private Integer retryFirstBackOff;

    @Value("${webclient.http.retry.max.backoff.seconds:50}")
    private Integer retryMaxBackOff;

    @Value("${webclient.http.read.timeout.seconds:20000}")
    private Integer readTimeout;

    @Value("${webclient.http.write.timeout.seconds:20000}")
    private Integer writeTimeout;

    @Value("${webclient.http.keep.alive.connection:true}")
    private Boolean keepAlive;

    @Value("${webclient.http.selector.thread.count:1}")
    private Integer selectorThreadCount;

    @Value("${webclient.http.worker.thread.count:4}")
    private Integer workerThreadCount;

    @Autowired
    private final WebClientSslHelper webClientSslHelper;

    @Bean
    @ConditionalOnBean(UntrustedWebClientHelper.class)
    public WebClient getUntrustedWebClient() {
        log.info("Entering getWebClient");
        WebClient.Builder webClientBuilder = WebClient.builder();
        WebClient webClient = null;
        try {
            HttpClient nettyHttpClient = getNettyBasedHttpClient();
            webClient = webClientBuilder
                            .baseUrl("http://localhost:7080")
                            .clientConnector(new ReactorClientHttpConnector(nettyHttpClient))
                            .build();
            log.info("-- WebClient successfully instantiated with Netty based HttpClient and base url");
        } catch (Exception ex) {
            log.error("Unable to instantiate WebClient. Error Message : {}, Error Cause : {}, Stracktrace : {}",
                ex.getMessage(), ex.getCause(), ex.getStackTrace());
        }
        log.info("Leaving getWebClient");

        return webClient;
    }

    /**
     * maxConnections : Set the options to use for configuring {@link ConnectionProvider} maximum connections per connection pool.
     * 		            Default to DEFAULT_POOL_MAX_CONNECTIONS}.
     * 		            - It is the maximum number of connections (per connection pool) before start pending
     *
     * 	maxIdleTime : Set the options to use for configuring {@link ConnectionProvider} max idle time (resolution: ms).
     * 		          Default to DEFAULT_POOL_MAX_IDLE_TIME} if specified otherwise - no max idle time.
     * 		          - Duration after which the channel will be closed when idle (resolution: ms)
     *
     *  maxLifeTime : Set the options to use for configuring {@link ConnectionProvider} max life time (resolution: ms).
     * 		         By default no max life time.
     * 		         - Duration after which the channel will be closed (resolution: ms)
     *
     * 	pendingAcquireMaxCount  : Set the options to use for configuring {@link ConnectionProvider} the maximum number of registered
     * 		                      requests for acquire to keep in a pending queue
     * 		                      When invoked with -1 the pending queue will not have upper limit.
     * 		                      Default to {@code 2 * max connections}.
     * 		                      - the maximum number of registered requests for acquire to keep
     * 		                        in a pending queue
     *
     *  pendingAcquireTimeout : Set the options to use for configuring {@link ConnectionProvider} acquire timeout (resolution: ms).
     * 		                    Default to DEFAULT_POOL_ACQUIRE_TIMEOUT.
     * 		                    - the maximum time after which a pending acquire
     * 		                    must complete or the {TimeoutException} will be thrown (resolution: ms)
     *
     * 	ChannelOption.CONNECT_TIMEOUT_MILLIS
     * 	ChannelOption.TCP_NODELAY
     *
     * 	readTimeout
     * 	writeTimeout
     *
     * 	keepAlive
     * 	wiretap
     * */
    private HttpClient getNettyBasedHttpClient() {
        HttpClient nettyHttpClient = null;
        try {
            ConnectionProvider connProvider = ConnectionProvider
                                                .builder("webclient-conn-pool")
                                                .maxConnections(maxConnections)
                                                /*.maxIdleTime()
                                                .maxLifeTime()
                                                .pendingAcquireMaxCount()*/
                                                .pendingAcquireTimeout(Duration.ofMillis(acquireTimeoutMillis))
                                                .build();

            log.info("""
                ConnectionProvider instantiated with max connections : {} and \
                acquire time out in millis : {}\
                """, maxConnections, acquireTimeoutMillis);

            nettyHttpClient = HttpClient
                                .create(connProvider)
                                .secure(sslContextSpec -> sslContextSpec.sslContext(webClientSslHelper.getSslContext()))
                                .tcpConfiguration(tcpClient -> {
                                    LoopResources loop = LoopResources.create("webclient-event-loop",
                                        selectorThreadCount, workerThreadCount, Boolean.TRUE);

                                    return tcpClient
                                            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeoutMillis)
                                            .option(ChannelOption.TCP_NODELAY, true)
                                            .doOnConnected(connection -> {
                                                connection
                                                    .addHandlerLast(new ReadTimeoutHandler(readTimeout))
                                                    .addHandlerLast(new WriteTimeoutHandler(writeTimeout));
                                            })
                                            .runOn(loop);
                                })
                                .keepAlive(keepAlive)
                                .wiretap(Boolean.TRUE);

            log.info("""
                Netty based http client successfully instantiated with connectTimeoutMillis : {},\
                readTimeout : {} and writeTimeout : {}\
                """, connectionTimeoutMillis, readTimeout, writeTimeout);
        } catch (Exception ex) {
            log.error("Unable to instantiate Netty based HttpClient. Error Message : {}, Error Cause : {}, Stracktrace : {}",
                    ex.getMessage(), ex.getCause(), ex.getStackTrace());
        }
        return nettyHttpClient;
    }
}
