package com.its.RxWebClientDemo.infrastructure;

import com.its.RxWebClientDemo.service.exception.RemoteServiceUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.retry.Retry;
import reactor.retry.RetryContext;

import java.time.Duration;

@Service
@Slf4j
@RequiredArgsConstructor
public class DownstreamAdapterImpl implements DownstreamAdapter {
    @Autowired
    private final WebClient restWebClient;

    @Autowired
    private final WebClientConfig webClientConfig;

    @Override
    public Mono<String> generateAlias(String cardNo) {
        log.info("3 Entering generateAlias and fetching alias for cardNo = {} by invoking downstream system ", cardNo);
        Mono<String> cardAliasMono = null;
        try {
            cardAliasMono = restWebClient
                            .get()
                            .uri("/{cardNo}", cardNo)
                            .headers(this::populateHttpHeaders)
                            .retrieve()
                            .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
                                log.error("Client error from downstream system");
                                return Mono.error(new HttpClientErrorException(HttpStatus.BAD_REQUEST));
                            })
                            .bodyToMono(String.class)
                            .retryWhen(Retry
                                        .onlyIf(this::is5xxServerError)
                                        .exponentialBackoff(Duration.ofSeconds(webClientConfig.getRetryFirstBackOff()),
                                                Duration.ofSeconds(webClientConfig.getRetryMaxBackOff()))
                                        .retryMax(webClientConfig.getMaxRetryAttempts())
                                        .doOnRetry(this::processOnRetry)
                            )
                            .doOnError(this::processInvocationErrors);

            log.info("Card alias received from backend : {} ", cardAliasMono);
            return cardAliasMono;
        } catch (Exception ex) {
            log.error("Exception occurred - exception message : {} & stacktrace : {} whilst invoking downstream system " +
                    "for card = {} ", ex.getMessage(), ex.getStackTrace(), cardNo);
            throw new RemoteServiceUnavailableException("Service Unavailable");
        }
    }

    private void processInvocationErrors(Throwable ex) {
        log.error("Downstream Invocation Error - Downstream failure cause : {}, Throwable Class : {}, Stacktrace : {}, " +
                "   Exception message : {}", ex.getCause(), ex.getClass(), ex.getStackTrace(), ex.getMessage());
        if(ex instanceof HttpClientErrorException || ex instanceof HttpServerErrorException)  {
            log.error("Downstream exception response : {}", ((HttpStatusCodeException) ex).getResponseBodyAsString());
            throw new RemoteServiceUnavailableException("Service Unavailable");
        } else {
            log.error("Downstream generic exception {}", ex.getMessage());
            throw new RemoteServiceUnavailableException("Service Unavailable");
        }
    }

    private boolean is5xxServerError(RetryContext<Object> retryContext) {
        return retryContext.exception() instanceof HttpClientErrorException ||
                retryContext.exception() instanceof HttpServerErrorException ||
                retryContext.exception() instanceof RuntimeException;
    }

    private void populateHttpHeaders(HttpHeaders httpHeaders) {
        log.debug("Populating http headers : {}", httpHeaders);
    }

    private void processOnRetry(RetryContext<Object> retryContext) {
        log.warn("Recovering from Downstream invocation failure by initiating Retry");
        log.info("Retrying Downstream invocation via WebClient. Retry configurations - " +
            "Max Retry attempts : {}, First back off duration : {}," + "Max back off duration : {}",
                webClientConfig.getMaxRetryAttempts(), webClientConfig.getRetryFirstBackOff(), webClientConfig.getRetryMaxBackOff());
    }

}
