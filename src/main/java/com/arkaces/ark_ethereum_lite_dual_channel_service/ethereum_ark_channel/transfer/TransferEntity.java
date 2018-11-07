package com.arkaces.ark_ethereum_lite_dual_channel_service.ethereum_ark_channel.transfer;

import com.arkaces.ark_ethereum_lite_dual_channel_service.ethereum_ark_channel.contract.ContractEntity;
import lombok.Data;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity(name = "ethereumArkChannel.TransferEntity")
@Table(name = "transfers", schema = "ethereum_ark_channel")
public class TransferEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pid;

    private String id;
    private LocalDateTime createdAt;
    private String status;
    private String ethTransactionId;

    @Column(precision = 20, scale = 8)
    private BigDecimal ethAmount;

    @Column(precision = 20, scale = 8)
    private BigDecimal ethToArkRate;

    @Column(precision = 20, scale = 8)
    private BigDecimal ethFlatFee;

    @Column(precision = 20, scale = 8)
    private BigDecimal ethPercentFee;

    @Column(precision = 20, scale = 8)
    private BigDecimal ethTotalFee;

    @Column(precision = 20, scale = 8)
    private BigDecimal arkSendAmount;

    private String arkTransactionId;

    private String returnEthTransactionId;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "contract_pid")
    private ContractEntity contractEntity;
}
