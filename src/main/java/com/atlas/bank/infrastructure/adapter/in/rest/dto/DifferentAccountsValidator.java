package com.atlas.bank.infrastructure.adapter.in.rest.dto;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Objects;

public class DifferentAccountsValidator implements ConstraintValidator< DifferentAccounts, TransferRequest > {

    @Override
    public boolean isValid( TransferRequest transferRequest, ConstraintValidatorContext context ) {

        if ( Objects.isNull( transferRequest.getFromAccountId() ) || Objects.isNull( transferRequest.getToAccountId() ) ) {
            return true;
        }

        return !transferRequest.getFromAccountId().equals( transferRequest.getToAccountId() );

    }

}