package com.atlas.bank.infrastructure.adapter.in.rest;

import com.atlas.bank.application.port.in.CreateAccountUseCase;
import com.atlas.bank.application.port.in.GetAccountUseCase;
import com.atlas.bank.application.port.in.ListAccountUseCase;
import com.atlas.bank.infrastructure.adapter.in.rest.dto.AccountMapper;
import com.atlas.bank.infrastructure.adapter.in.rest.dto.AccountResponse;
import com.atlas.bank.infrastructure.adapter.in.rest.dto.CreateAccountRequest;
import com.atlas.bank.infrastructure.adapter.in.rest.dto.DashBoardResponse;
import com.atlas.bank.domain.model.account.Account;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping( "/api/v1/accounts" )
public class AccountController {

    private final CreateAccountUseCase createAccountUseCase;
    private final ListAccountUseCase listAccountUseCase;
    private final GetAccountUseCase getAccountUseCase;

    private final AccountMapper accountMapper;

    private final AccountDashboardFacade dashboardFacade;

    @GetMapping( "/{id}/dashboard" )
    public ResponseEntity< DashBoardResponse > getDashboard( @PathVariable Long id ) {
        return ResponseEntity.ok( dashboardFacade.getDashboard( id ) );
    }

    @PostMapping
    public ResponseEntity< AccountResponse > create( @Valid @RequestBody CreateAccountRequest request ) {

        Account account = accountMapper.toEntity( request );
        Account saved = createAccountUseCase.create( account );

        return ResponseEntity.status( HttpStatus.CREATED ).body( accountMapper.toResponse( saved ) );

    }

    @GetMapping
    public ResponseEntity< List< AccountResponse > > findAll() {

        List< AccountResponse > responses = listAccountUseCase
                .findAll()
                .stream()
                .map( accountMapper::toResponse )
                .toList();

        return ResponseEntity.status( HttpStatus.OK ).body( responses );

    }

    @GetMapping( "/{id}" )
    public ResponseEntity< AccountResponse > findById( @PathVariable Long id ) {
        return ResponseEntity.ok( accountMapper.toResponse( getAccountUseCase.findById( id ) ) );
    }

}