package com.its.RxWebClientDemo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CardEntity implements Serializable {
    private Long id;
    private String issuingNetwork;
    private String cardNumber;
    private String name;
    private String expiryDate;
    private String alias;
}
