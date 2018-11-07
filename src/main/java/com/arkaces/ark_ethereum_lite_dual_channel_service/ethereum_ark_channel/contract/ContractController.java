package com.arkaces.ark_ethereum_lite_dual_channel_service.ethereum_ark_channel.contract;

import com.arkaces.aces_server.aces_service.contract.Contract;
import com.arkaces.aces_server.aces_service.contract.ContractStatus;
import com.arkaces.aces_server.aces_service.contract.CreateContractRequest;
import com.arkaces.aces_server.aces_service.error.ServiceErrorCodes;
import com.arkaces.aces_server.common.error.NotFoundException;
import com.arkaces.aces_server.common.identifer.IdentifierGenerator;
import com.arkaces.ark_ethereum_lite_dual_channel_service.ethereum.EthereumService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController("ethereumArkChannel.contractController")
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
@ConditionalOnProperty("ethereumArkChannel.enabled")
@RequestMapping(path = "${ethereumArkChannel.urlPrefix}")
public class ContractController {
    
    private final IdentifierGenerator identifierGenerator;
    @Qualifier("ethereumArkChannel.contractRepository")
    private final ContractRepository contractRepository;
    @Qualifier("ethereumArkChannel.contractMapper")
    private final ContractMapper contractMapper;
    @Qualifier("ethereumArkChannel.contractRequestValidator")
    private final CreateContractRequestValidator createContractRequestValidator;
    private final EthereumService ethereumService;

    @PostMapping("/contracts")
    public Contract<Results> postContract(@RequestBody CreateContractRequest<Arguments> createContractRequest) {
        createContractRequestValidator.validate(createContractRequest);

        String depositEthAddressPassphrase = identifierGenerator.generate();
        String depositEthAddress = ethereumService.createAddress(depositEthAddressPassphrase);

        ContractEntity contractEntity = new ContractEntity();
        contractEntity.setId(identifierGenerator.generate());
        contractEntity.setCorrelationId(createContractRequest.getCorrelationId());
        contractEntity.setReturnEthAddress(createContractRequest.getArguments().getReturnEthAddress());
        contractEntity.setRecipientArkAddress(createContractRequest.getArguments().getRecipientArkAddress());
        contractEntity.setCreatedAt(LocalDateTime.now());
        contractEntity.setStatus(ContractStatus.EXECUTED);
        contractEntity.setDepositEthAddress(depositEthAddress);
        contractEntity.setDepositEthAddressPassphrase(depositEthAddressPassphrase);
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
