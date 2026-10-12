package com.atlas.bank.infrastructure.adapter.out.fraud;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ExternalFraudResponse {

    private String riskLevel;
    private double score;
    private String recommendation;

}