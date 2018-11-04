package com.arkaces.ark_ethereum_lite_dual_channel_service.ark_ethereum_channel.contract;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("arkEthereumChannel.contractRepository")
public interface ContractRepository extends JpaRepository<ContractEntity, Long> {

    ContractEntity findOneById(String id);

    ContractEntity findOneByCorrelationId(String correlationId);

}
