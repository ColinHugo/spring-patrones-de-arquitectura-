package com.atlas.bank.transaction.service.transfer;

import com.atlas.bank.account.exception.AccountNotFoundException;
import com.atlas.bank.account.model.Account;
import com.atlas.bank.shared.model.Money;
import com.atlas.bank.transaction.model.Transaction;
import com.atlas.bank.account.repository.AccountRepository;
import com.atlas.bank.transaction.repository.TransactionRepository;
import com.atlas.bank.transaction.service.event.TransactionExecutedEvent;
import com.atlas.bank.transaction.service.factory.TransactionFactory;
import com.atlas.bank.transaction.service.fee.FeeCalculator;
import com.atlas.bank.transaction.validation.chain.TransferValidator;
import jakarta.transaction.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class TransferService extends TransactionProcessor< TransferContext > implements ITransferService {

    private final ApplicationEventPublisher eventPublisher;

    private final AccountRepository accountRepository;

    private final List< FeeCalculator > feeCalculators;
    private final List< TransferValidator > validators;

    public TransferService( TransactionRepository transactionRepository, ApplicationEventPublisher eventPublisher,
                            AccountRepository accountRepository, List< FeeCalculator > feeCalculators,
                            List< TransferValidator > validators ) {

        super( transactionRepository );
        this.eventPublisher = eventPublisher;
        this.accountRepository = accountRepository;
        this.feeCalculators = feeCalculators;
        this.validators = validators;
    }

    @Override
    @Transactional
    public Transaction execute( Long fromId, Long toId, BigDecimal amount ) {

        Account from = accountRepository
                .findById( fromId )
                .orElseThrow( () -> new AccountNotFoundException( fromId ) );

        Account to = accountRepository
                .findById( toId )
                .orElseThrow( () -> new AccountNotFoundException( toId ) );

        Transaction transaction = process( new TransferContext( from, to, amount ) );

        transaction.advanceTo( transaction.getState().validate() );
        transaction.advanceTo( transaction.getState().execute() );
        transactionRepository.save( transaction );

        eventPublisher.publishEvent( new TransactionExecutedEvent(
                transaction.getId(),
                transaction.getType().name(),
                transaction.getSourceAccountId(),
                transaction.getTargetAccountId(),
                transaction.getAmount(),
                transaction.getFee()
        ) );

        return transaction;

    }

    @Override
    protected void validate( TransferContext context ) {
        validators.forEach( validator -> validator.validate( context ) );
    }

    @Override
    protected BigDecimal calculateFee( TransferContext context ) {

        return feeCalculators
                .stream()
                .filter( fc -> fc.supports( context.from().getType() ) )
                .findFirst()
                .orElseThrow( () -> new RuntimeException( "No hay calculador para el tipo " + context.from().getType() ) )
                .calculate( context.amount() );

    }

    @Override
    protected void execute( TransferContext context, BigDecimal fee ) {

        BigDecimal newFromBalance = context.from().getBalance().getAmount().subtract( context.amount() ).subtract( fee );
        BigDecimal newToBalance = context.to().getBalance().getAmount().add( context.amount() );

        context.from().setBalance( Money.of( newFromBalance, context.from().getBalance().getCurrency() ) );
        context.to().setBalance( Money.of( newToBalance, context.to().getBalance().getCurrency() ) );

        accountRepository.save( context.from() );
        accountRepository.save( context.to() );

    }

    @Override
    protected Transaction save( TransferContext context, BigDecimal fee ) {
        Transaction transaction = TransactionFactory.createTransfer( context, fee );
        return transactionRepository.save( transaction );
    }

}