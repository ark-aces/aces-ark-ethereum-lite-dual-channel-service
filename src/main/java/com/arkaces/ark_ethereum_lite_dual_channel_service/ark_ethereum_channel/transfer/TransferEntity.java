package com.arkaces.ark_ethereum_lite_dual_channel_service.ark_ethereum_channel.transfer;

import com.arkaces.ark_ethereum_lite_dual_channel_service.ark_ethereum_channel.contract.ContractEntity;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "transfers", schema = "ark_ethereum_channel")
public class TransferEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pid;

    private String id;
    private LocalDateTime createdAt;
    private String status;
    private String arkTransactionId;

    @Column(precision = 20, scale = 8)
    private BigDecimal arkAmount;

    @Column(precision = 20, scale = 8)
    private BigDecimal arkToEthRate;

    @Column(precision = 20, scale = 8)
    private BigDecimal arkFlatFee;

    @Column(precision = 20, scale = 8)
    private BigDecimal arkPercentFee;

    @Column(precision = 20, scale = 8)
    private BigDecimal arkTotalFee;

    @Column(precision = 20, scale = 8)
    private BigDecimal ethSendAmount;

    private String ethTransactionId;

    private String returnArkTransactionId;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "contract_pid")
    private ContractEntity contractEntity;
}
