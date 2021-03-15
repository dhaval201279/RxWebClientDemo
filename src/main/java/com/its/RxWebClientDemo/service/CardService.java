package com.its.RxWebClientDemo.service;

import com.its.RxWebClientDemo.entity.CardEntity;
import com.its.RxWebClientDemo.infrastructure.DownstreamAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class CardService {

    @Autowired
    private final DownstreamAdapter downstreamAdapter;

    public Mono<CardEntity> enroll(CardEntity aCard) {
        log.info("Entering enroll with cardId : {}",aCard.getCardNumber());
        String cardNo = aCard.getCardNumber();

        return downstreamAdapter
                .generateAlias(cardNo)
                .flatMap(cardAlias -> {
                    aCard.setAlias(cardAlias);
                    return Mono.just(aCard);
                });
    }
}
