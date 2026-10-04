package com.atlas.bank.account.controller;

import com.atlas.bank.account.dto.AccountMapper;
import com.atlas.bank.account.dto.AccountResponse;
import com.atlas.bank.account.dto.CreateAccountRequest;
import com.atlas.bank.account.dto.DashBoardResponse;
import com.atlas.bank.account.service.AccountDashboardFacade;
import com.atlas.bank.account.service.IAccountService;
import com.atlas.bank.account.model.Account;
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

    private final IAccountService accountService;

    private final AccountMapper accountMapper;

    private final AccountDashboardFacade dashboardFacade;

    @GetMapping( "/{id}/dashboard" )
    public ResponseEntity< DashBoardResponse > getDashboard( @PathVariable Long id ) {
        return ResponseEntity.ok( dashboardFacade.getDashboard( id ) );
    }

    @PostMapping
    public ResponseEntity< AccountResponse > create( @Valid @RequestBody CreateAccountRequest request ) {

        Account account = accountMapper.toEntity( request );
        Account saved = accountService.create( account );

        return ResponseEntity.status( HttpStatus.CREATED ).body( accountMapper.toResponse( saved ) );

    }

    @GetMapping
    public ResponseEntity< List< AccountResponse > > findAll() {

        List< AccountResponse > responses = accountService.findAll()
                .stream()
                .map( accountMapper::toResponse )
                .toList();

        return ResponseEntity.status( HttpStatus.OK ).body( responses );

    }

    @GetMapping( "/{id}" )
    public ResponseEntity< AccountResponse > findById( @PathVariable Long id ) {
        return ResponseEntity.ok( accountMapper.toResponse( accountService.findById( id ) ) );
    }

}