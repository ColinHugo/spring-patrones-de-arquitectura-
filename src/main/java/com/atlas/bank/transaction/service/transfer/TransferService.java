package com.atlas.bank.transaction.service.transfer;

import com.atlas.bank.account.exception.AccountNotFoundException;
import com.atlas.bank.account.model.Account;
import com.atlas.bank.account.model.AccountStatus;
import com.atlas.bank.transaction.exception.AccountNotActiveException;
import com.atlas.bank.transaction.exception.InsufficientFundsException;
import com.atlas.bank.transaction.model.Transaction;
import com.atlas.bank.account.repository.AccountRepository;
import com.atlas.bank.transaction.repository.TransactionRepository;
import com.atlas.bank.transaction.service.event.TransactionExecutedEvent;
import com.atlas.bank.transaction.service.exception.FraudCheckException;
import com.atlas.bank.transaction.service.factory.TransactionFactory;
import com.atlas.bank.transaction.service.fee.FeeCalculator;
import com.atlas.bank.transaction.service.fraud.FraudCheckResult;
import com.atlas.bank.transaction.service.fraud.FraudChecker;
import jakarta.transaction.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class TransferService extends TransactionProcessor< TransferContext > implements ITransferService {

    private final ApplicationEventPublisher eventPublisher;

    private final AccountRepository accountRepository;

    private final FraudChecker fraudChecker;

    private final List< FeeCalculator > feeCalculators;

    public TransferService( TransactionRepository transactionRepository, ApplicationEventPublisher eventPublisher,
            AccountRepository accountRepository, FraudChecker fraudChecker, List< FeeCalculator > feeCalculators ) {

        super( transactionRepository );
        this.eventPublisher = eventPublisher;
        this.accountRepository = accountRepository;
        this.fraudChecker = fraudChecker;
        this.feeCalculators = feeCalculators;
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

        if ( context.from().getStatus() != AccountStatus.ACTIVE ) {
            throw new AccountNotActiveException( context.from().getId(), context.from().getStatus().name() );
        }

        if ( context.to().getStatus() != AccountStatus.ACTIVE  ) {
            throw new AccountNotActiveException( context.to().getId(), context.to().getStatus().name() );
        }

        if ( context.from().getBalance().compareTo( context.amount() ) < 0 ) {
            throw new InsufficientFundsException( context.from().getId(), context.from().getBalance(), context.amount() );
        }

        FraudCheckResult fraudCheckResult = fraudChecker.check( context.from().getId(), context.amount() );

        if ( fraudCheckResult.blocked() ) {
            throw new FraudCheckException( fraudCheckResult.reason() );
        }

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

        context.from().setBalance( context.from().getBalance().subtract( context.amount() ).subtract( fee ) );
        context.to().setBalance( context.to().getBalance().add( context.amount() ) );
        accountRepository.save( context.from() );
        accountRepository.save( context.to() );

    }

    @Override
    protected Transaction save( TransferContext context, BigDecimal fee ) {
        Transaction transaction = TransactionFactory.createTransfer( context, fee );
        return transactionRepository.save( transaction );
    }

}