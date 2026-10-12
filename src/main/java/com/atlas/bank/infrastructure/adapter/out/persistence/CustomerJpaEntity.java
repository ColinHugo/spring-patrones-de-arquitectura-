package com.atlas.bank.infrastructure.adapter.out.persistence;

import com.atlas.bank.domain.model.customer.CustomerStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode( onlyExplicitlyIncluded = true )
@Table( name = "customers" )
@Entity
public class CustomerJpaEntity {

    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @EqualsAndHashCode.Include
    private Long id;

    @Column( nullable = false )
    private String name;

    @Embedded
    @AttributeOverride(
            name = "value",
            column = @Column( name = "email", nullable = false, unique = true )
    )
    private Email email;

    @Enumerated( EnumType.STRING )
    @Column( nullable = false, length = 20 )
    private CustomerStatus status;

    @Column( name = "created_at", nullable = false, updatable = false )
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {

        this.createdAt = LocalDateTime.now();

        if ( status == null ) {
            status = CustomerStatus.ACTIVE;
        }

    }

}