package com.arkaces.ark_ethereum_lite_dual_channel_service.ethereum_ark_channel.contract;

import com.arkaces.aces_server.aces_service.contract.CreateContractRequest;
import com.arkaces.aces_server.common.error.ValidatorException;
import lombok.RequiredArgsConstructor;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Base58;
import org.bitcoinj.core.NetworkParameters;
import org.bouncycastle.util.encoders.DecoderException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CreateContractRequestValidator {
    
    private final ContractRepository contractRepository;
    private final NetworkParameters networkParameters;

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
        
        String recipientBtcAddress = createContractRequest.getArguments().getRecipientBtcAddress();
        if (StringUtils.isEmpty(recipientBtcAddress)) {
            bindingResult.rejectValue("arguments.recipientBtcAddress", FieldErrorCodes.REQUIRED, "Recipient BTC address required.");
        } else {
            try {
                new Address(networkParameters, recipientBtcAddress);
            } catch (AddressFormatException e) {
                bindingResult.rejectValue(
                        "arguments.recipientBtcAddress",
                        FieldErrorCodes.INVALID_BTC_ADDRESS_CHECKSUM,
                        "Invalid BTC address checksum."
                );
            } catch (DecoderException e) {
                bindingResult.rejectValue(
                        "arguments.recipientBtcAddress",
                        FieldErrorCodes.INVALID_BTC_ADDRESS,
                        "Invalid BTC address."
                );
            }
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
