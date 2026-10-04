package com.atlas.bank.account.model;

import com.atlas.bank.shared.model.Currency;
import com.atlas.bank.shared.model.Money;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode( onlyExplicitlyIncluded = true )
@Entity
@Table( name = "accounts" )
public class Account {

    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @EqualsAndHashCode.Include
    private Long id;

    @Column( name = "account_number", nullable = false, unique = true )
    private String accountNumber;

    @Column( name = "owner_name", nullable = false )
    private String ownerName;

    @Column( nullable = false )
    private String email;

    @Enumerated( EnumType.STRING )
    @Column( nullable = false, length = 20 )
    private AccountType type;

    @Embedded
    @AttributeOverrides( {
            @AttributeOverride(
                    name = "amount",
                    column = @Column( name = "balance", nullable = false )
            ),
            @AttributeOverride(
                    name = "currency",
                    column = @Column( name = "currency", nullable = false, length = 3 )
            )
    } )
    private Money balance;

    @Enumerated( EnumType.STRING )
    @Column( nullable = false, length = 20 )
    private AccountStatus status;

    @Column( name = "created_at", nullable = false, updatable = false )
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {

        this.createdAt = LocalDateTime.now();

        if ( Objects.isNull( status ) ) {
            status = AccountStatus.ACTIVE;
        }

        if ( Objects.isNull( balance ) ) {
            balance = Money.zero( Currency.MXN );
        }

    }

}