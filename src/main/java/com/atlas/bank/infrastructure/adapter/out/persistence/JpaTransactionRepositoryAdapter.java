package com.atlas.bank.infrastructure.adapter.out.persistence;

import com.atlas.bank.application.port.out.TransactionRepositoryPort;
import com.atlas.bank.domain.model.transaction.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class JpaTransactionRepositoryAdapter implements TransactionRepositoryPort {

    private final ApplicationEventPublisher eventPublisher;

    private final SpringDataTransactionRepository jpaRepository;

    private final TransactionPersistenceMapper mapper;

    @Override
    public Transaction save( Transaction transaction ) {

        transaction.initDefaults();

        TransactionJpaEntity entity = mapper.toJpaEntity( transaction );
        TransactionJpaEntity saved = jpaRepository.save( entity );

        Transaction result = mapper.toDomain( saved );

        transaction.getDomainEvents().forEach( eventPublisher::publishEvent );
        transaction.clearDomainEvents();

        return result;

    }

    @Override
    public List< Transaction > findBySourceAccountIdOrTargetAccountId( Long sourceId, Long targetId ) {
        return jpaRepository.findBySourceAccountIdOrTargetAccountId( sourceId, targetId )
                .stream()
                .map( mapper::toDomain )
                .toList();
    }

}