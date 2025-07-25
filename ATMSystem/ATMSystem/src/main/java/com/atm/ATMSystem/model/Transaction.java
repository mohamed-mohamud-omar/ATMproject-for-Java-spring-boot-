package com.atm.ATMSystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String type; // e.g., WITHDRAW, DEPOSIT

    private Double amount;

    private String description;

    private LocalDateTime timestamp = LocalDateTime.now();
}
