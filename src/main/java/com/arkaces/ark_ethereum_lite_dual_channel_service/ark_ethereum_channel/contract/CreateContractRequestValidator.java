package com.arkaces.ark_ethereum_lite_dual_channel_service.ark_ethereum_channel.contract;

import com.arkaces.aces_server.aces_service.contract.CreateContractRequest;
import com.arkaces.aces_server.common.error.ValidatorException;
import lombok.RequiredArgsConstructor;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Base58;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

@Service("arkEthereumChannel.createContractRequestValidator")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CreateContractRequestValidator {

    @Qualifier("arkEthereumChannel.contractRepository")
    private final ContractRepository contractRepository;

    public void validate(CreateContractRequest<Arguments> createContractRequest) {
        BindingResult bindingResult = new BeanPropertyBindingResult(createContractRequest, "createContractRequest");
        
        String correlationId = createContractRequest.getCorrelationId();
        if (! StringUtils.isEmpty(correlationId)) {
            ContractEntity contractEntity = contractRepository.findOneByCorrelationId(correlationId);
            if (contractEntity != null) {
                bindingResult.rejectValue("correlationId", FieldErrorCodes.DUPLICATE_CORRELATION_ID, 
                    "A contract with the given correlation ID already exists.");
            }
        }
        
        String recipientEthAddress = createContractRequest.getArguments().getRecipientEthAddress();
        if (StringUtils.isEmpty(recipientEthAddress)) {
            bindingResult.rejectValue("arguments.recipientEthAddress", FieldErrorCodes.REQUIRED, "Recipient ETH address required.");
        } else {
            // todo: validate eth address
        }

        String returnArkAddress = createContractRequest.getArguments().getReturnArkAddress();
        if (! StringUtils.isEmpty(returnArkAddress)) {
            try {
                Base58.decodeChecked(returnArkAddress);
            } catch (AddressFormatException exception) {
                if (exception.getMessage().equals("Checksum does not validate")) {
                    bindingResult.rejectValue(
                            "arguments.returnArkAddress",
                            FieldErrorCodes.INVALID_ARK_ADDRESS_CHECKSUM,
                            "Invalid ARK address checksum."
                    );
                } else {
                    bindingResult.rejectValue(
                            "arguments.returnArkAddress",
                            FieldErrorCodes.INVALID_ARK_ADDRESS,
                            "Invalid ARK address."
                    );
                }
            }
        }

        if (bindingResult.hasErrors()) {
            throw new ValidatorException(bindingResult);
        }
    }
    
}
