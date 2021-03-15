package com.its.RxWebClientDemo.controller;

import com.its.RxWebClientDemo.entity.CardEntity;
import com.its.RxWebClientDemo.service.CardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
public class CardLifecycleController {
    private final CardService cardService;

    public CardLifecycleController(CardService cardService) {
        this.cardService = cardService;
    }

    @PostMapping("/card")
    public Mono<CardEntity> addCard(@RequestBody CardEntity aCard) {
        log.info("Entering and leaving CardLifecycleController : addCard after saving card with no. - {}  ",
                aCard.getCardNumber());
        return cardService.enroll(aCard);
    }
}
