package com.arkaces.ark_ethereum_lite_dual_channel_service.ethereum_ark_channel.contract;

import com.arkaces.ark_ethereum_lite_dual_channel_service.ark_ethereum_channel.transfer.TransferEntity;
import lombok.Data;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "contracts", schema = "ethereum_ark_channel")
public class ContractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pid;

    private String id;
    private String correlationId;
    private String status;
    private LocalDateTime createdAt;
    private String recipientArkAddress;
    private String returnEthAddress;
    private String depositEthAddress;
    private String depositEthAddressPassphrase;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "contractEntity")
    private List<TransferEntity> transferEntities = new ArrayList<>();
    
}
