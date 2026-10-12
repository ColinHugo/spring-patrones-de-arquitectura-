package com.atlas.bank.infrastructure.adapter.in.rest;

import com.atlas.bank.application.port.in.GetAccountUseCase;
import com.atlas.bank.application.port.in.GetTransactionByAccountUseCase;
import com.atlas.bank.infrastructure.adapter.in.rest.dto.DashBoardResponse;
import com.atlas.bank.domain.model.account.Account;
import com.atlas.bank.infrastructure.adapter.in.rest.dto.TransactionMapper;
import com.atlas.bank.infrastructure.adapter.in.rest.dto.TransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountDashboardFacade {

    private final GetAccountUseCase getAccountUseCase;
    private final GetTransactionByAccountUseCase getTransactionByAccountUseCase;
    private final TransactionMapper transactionMapper;

    public DashBoardResponse getDashboard( Long accountId ) {

        Account account = getAccountUseCase.findById( accountId );

        List< TransactionResponse > transactions = getTransactionByAccountUseCase
                .getByAccountId( accountId )
                .stream()
                .map( transactionMapper::toResponse )
                .toList();

        return DashBoardResponse
                .builder()
                .accountId( account.getId() )
                .accountNumber( account.getAccountNumber() )
                .ownerName( account.getOwnerName() )
                .type( account.getType().name() )
                .balance( account.getBalance().getAmount() )
                .status( account.getStatus().name() )
                .recentTransactions( transactions )
                .build();

    }

}