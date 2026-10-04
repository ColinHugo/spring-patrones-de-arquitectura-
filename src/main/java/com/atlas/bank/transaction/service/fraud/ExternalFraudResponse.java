package com.atlas.bank.transaction.service.fraud;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ExternalFraudResponse {

    private String riskLevel;
    private double score;
    private String recommendation;

}