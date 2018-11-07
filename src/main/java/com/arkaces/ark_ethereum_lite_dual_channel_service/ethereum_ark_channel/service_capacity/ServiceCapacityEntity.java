package com.arkaces.ark_ethereum_lite_dual_channel_service.ethereum_ark_channel.service_capacity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity(name = "ethereumArkChannel.ServiceCapacityEntity")
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
