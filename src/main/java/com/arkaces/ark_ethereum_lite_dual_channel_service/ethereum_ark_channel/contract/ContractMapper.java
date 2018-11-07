package com.arkaces.ark_ethereum_lite_dual_channel_service.ethereum_ark_channel.contract;

import com.arkaces.aces_server.aces_service.contract.Contract;
import com.arkaces.ark_ethereum_lite_dual_channel_service.ethereum_ark_channel.transfer.TransferMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.util.stream.Collectors;

@Service("ethereumArkChannel.contractMapper")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ContractMapper {

    @Qualifier("ethereumArkChannel.transferMapper")
    private final TransferMapper transferMapper;
    
    public Contract<Results> map(ContractEntity contractEntity) {
        Contract<Results> contract = new Contract<>();
        contract.setId(contractEntity.getId());
        contract.setCorrelationId(contractEntity.getCorrelationId());
        contract.setCreatedAt(contractEntity.getCreatedAt().atOffset(ZoneOffset.UTC).toString());
        contract.setStatus(contractEntity.getStatus());

        Results results = new Results();
        results.setDepositEthAddress(contractEntity.getDepositEthAddress());
        results.setReturnEthAddress(contractEntity.getReturnEthAddress());
        results.setRecipientArkAddress(contractEntity.getRecipientArkAddress());
        results.setTransfers(
            contractEntity.getTransferEntities().stream()
                .map(transferMapper::map)
                .collect(Collectors.toList())
        );
        
        contract.setResults(results);
        
        return contract;
    }
}
