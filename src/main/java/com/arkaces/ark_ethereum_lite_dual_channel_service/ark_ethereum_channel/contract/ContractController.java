package com.arkaces.ark_ethereum_lite_dual_channel_service.ark_ethereum_channel.contract;

import ark_java_client.ArkClient;
import com.arkaces.aces_server.aces_service.contract.Contract;
import com.arkaces.aces_server.aces_service.contract.ContractStatus;
import com.arkaces.aces_server.aces_service.contract.CreateContractRequest;
import com.arkaces.aces_server.aces_service.error.ServiceErrorCodes;
import com.arkaces.aces_server.common.error.NotFoundException;
import com.arkaces.aces_server.common.identifer.IdentifierGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController("arkEthereumChannel.contractController")
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
@ConditionalOnProperty("arkEthereumChannel.enabled")
@RequestMapping(path = "/${arkEthereumChannel.urlPrefix}")
public class ContractController {
    
    private final IdentifierGenerator identifierGenerator;
    @Qualifier("arkEthereumChannel.contractRepository")
    private final ContractRepository contractRepository;
    @Qualifier("arkEthereumChannel.contractMapper")
    private final ContractMapper contractMapper;
    @Qualifier("arkEthereumChannel.createContractRequestValidator")
    private final CreateContractRequestValidator createContractRequestValidator;
    private final ArkClient arkClient;

    @PostMapping("/contracts")
    public Contract<Results> postContract(@RequestBody CreateContractRequest<Arguments> createContractRequest) {
        createContractRequestValidator.validate(createContractRequest);

        String depositArkAddressPassphrase = identifierGenerator.generate();
        String depositArkAddress = arkClient.getAddress(depositArkAddressPassphrase);

        ContractEntity contractEntity = new ContractEntity();
        contractEntity.setId(identifierGenerator.generate());
        contractEntity.setCorrelationId(createContractRequest.getCorrelationId());
        contractEntity.setReturnArkAddress(createContractRequest.getArguments().getReturnArkAddress());
        contractEntity.setRecipientEthAddress(createContractRequest.getArguments().getRecipientEthAddress());
        contractEntity.setCreatedAt(LocalDateTime.now());
        contractEntity.setStatus(ContractStatus.EXECUTED);
        contractEntity.setDepositArkAddress(depositArkAddress);
        contractEntity.setDepositArkAddressPassphrase(depositArkAddressPassphrase);
        contractRepository.save(contractEntity);

        return contractMapper.map(contractEntity);
    }
    
    @GetMapping("/contracts/{contractId}")
    public Contract<Results> getContract(@PathVariable String contractId) {
        ContractEntity contractEntity = contractRepository.findOneById(contractId);
        if (contractEntity == null) {
            throw new NotFoundException(ServiceErrorCodes.CONTRACT_NOT_FOUND, "Contract not found with id = " + contractId);
        }
        
        return contractMapper.map(contractEntity);
    }

}
