package com.arkaces.ark_ethereum_lite_dual_channel_service.ethereum_ark_channel.contract;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("ethereumArkChannel.contractRepository")
public interface ContractRepository extends JpaRepository<ContractEntity, Long> {

    ContractEntity findOneById(String id);

    ContractEntity findOneByCorrelationId(String correlationId);

}
