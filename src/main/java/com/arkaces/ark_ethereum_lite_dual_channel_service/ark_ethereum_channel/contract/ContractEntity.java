package com.arkaces.ark_ethereum_lite_dual_channel_service.ark_ethereum_channel.contract;

import com.arkaces.ark_ethereum_lite_dual_channel_service.ark_ethereum_channel.transfer.TransferEntity;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity(name = "arkEthereumChannel.ContractEntity")
@Table(name = "contracts", schema = "ark_ethereum_channel")
public class ContractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pid;

    private String id;
    private String correlationId;
    private String status;
    private LocalDateTime createdAt;
    private String recipientEthAddress;
    private String returnArkAddress;
    private String depositArkAddress;
    private String depositArkAddressPassphrase;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "contractEntity")
    private List<TransferEntity> transferEntities = new ArrayList<>();
    
}
