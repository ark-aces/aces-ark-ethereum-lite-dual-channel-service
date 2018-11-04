package com.arkaces.ark_ethereum_lite_dual_channel_service.ark_ethereum_channel.service_capacity;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "service_capacities", schema = "ethereum_ark_channel")
public class ServiceCapacityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pid;
    private BigDecimal availableAmount;
    private BigDecimal unsettledAmount;
    private BigDecimal totalAmount;
    private String unit;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
