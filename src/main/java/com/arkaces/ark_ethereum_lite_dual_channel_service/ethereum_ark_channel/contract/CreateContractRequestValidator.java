package com.arkaces.ark_ethereum_lite_dual_channel_service.ethereum_ark_channel.contract;

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

@Service("ethereumArkChannel.createContractRequestValidator")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CreateContractRequestValidator {

    @Qualifier("ethereumArkChannel.contractRepository")
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
        
        String recipientArkAddress = createContractRequest.getArguments().getRecipientArkAddress();
        if (StringUtils.isEmpty(recipientArkAddress)) {
            bindingResult.rejectValue("arguments.recipientArkAddress", FieldErrorCodes.REQUIRED, "Recipient ARK address required.");
        } else {
            try {
                Base58.decodeChecked(recipientArkAddress);
            } catch (AddressFormatException exception) {
                if (exception.getMessage().equals("Checksum does not validate")) {
                    bindingResult.rejectValue(
                            "arguments.recipientArkAddress",
                            FieldErrorCodes.INVALID_ARK_ADDRESS_CHECKSUM,
                            "Invalid ARK address checksum."
                    );
                } else {
                    bindingResult.rejectValue(
                            "arguments.recipientArkAddress",
                            FieldErrorCodes.INVALID_ARK_ADDRESS,
                            "Invalid ARK address."
                    );
                }
            }
        }

        String returnEthAddress = createContractRequest.getArguments().getReturnEthAddress();
        if (! StringUtils.isEmpty(returnEthAddress)) {
            // todo: validate eth address
        }

        if (bindingResult.hasErrors()) {
            throw new ValidatorException(bindingResult);
        }
    }
    
}
