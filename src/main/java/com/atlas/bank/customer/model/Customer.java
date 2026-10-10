package com.atlas.bank.customer.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode( onlyExplicitlyIncluded = true )
@Entity
public class Customer {

    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @EqualsAndHashCode.Include
    private Long id;

    @Column( nullable = false )
    private String name;

    @Column( unique = true, nullable = false )
    private String email;

    @Enumerated( EnumType.STRING )
    @Column( nullable = false, length = 20 )
    private CustomerStatus status;

    @Column( nullable = false, updatable = false )
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {

        this.createdAt = LocalDateTime.now();

        if ( this.status == null ) {
            this.status = CustomerStatus.ACTIVE;
        }

    }

    public boolean isActive() {
        return this.status == CustomerStatus.ACTIVE;
    }

}