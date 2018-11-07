package com.arkaces.ark_ethereum_lite_dual_channel_service.ethereum_ark_channel.transfer;

import com.arkaces.aces_server.common.identifer.IdentifierGenerator;
import com.arkaces.ark_ethereum_lite_dual_channel_service.Constants;
import com.arkaces.ark_ethereum_lite_dual_channel_service.ethereum.EthereumWeiService;
import com.arkaces.ark_ethereum_lite_dual_channel_service.ethereum.Transaction;
import com.arkaces.ark_ethereum_lite_dual_channel_service.ethereum_ark_channel.config.Config;
import com.arkaces.ark_ethereum_lite_dual_channel_service.ethereum_ark_channel.contract.ContractEntity;
import com.arkaces.ark_ethereum_lite_dual_channel_service.ethereum_ark_channel.contract.ContractRepository;
import com.arkaces.ark_ethereum_lite_dual_channel_service.ethereum_ark_channel.exchange_rate.ExchangeRateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EthTransactionEventHandler {

    @Qualifier("ethereumArkChannel.contractRepository")
    private final ContractRepository contractRepository;
    @Qualifier("ethereumArkChannel.transferRepository")
    private final TransferRepository transferRepository;
    private final IdentifierGenerator identifierGenerator;
    @Qualifier("ethereumArkChannel.exchangeRateService")
    private final ExchangeRateService exchangeRateService;
    @Qualifier("ethereumArkChannel.config")
    private final Config config;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final EthereumWeiService ethereumWeiService;

    @EventListener
    @Transactional
    public void handleEthTransactionEvent(NewEthTransactionEvent eventPayload) {
        String ethTransactionId = eventPayload.getTransactionId();

        log.info("Received Eth transaction event: " + ethTransactionId + " -> " + eventPayload.getTransaction());

        ContractEntity contractEntity = contractRepository.findById(eventPayload.getContractPid()).orElse(null);
        if (contractEntity == null) {
            log.info("Eth event has no corresponding contract: " + eventPayload);
            return;
        }

        log.info("Matched event for contract id " + contractEntity.getId() + " eth transaction id " + ethTransactionId);

        TransferEntity existingTransferEntity = transferRepository.findOneByEthTransactionId(ethTransactionId);
        if (existingTransferEntity != null) {
            log.info("Transfer for eth transaction " + ethTransactionId + " already exists with id " + existingTransferEntity.getId());
            return;
        }

        String transferId = identifierGenerator.generate();

        TransferEntity transferEntity = new TransferEntity();
        transferEntity.setId(transferId);
        transferEntity.setStatus(TransferStatus.NEW);
        transferEntity.setCreatedAt(LocalDateTime.now());
        transferEntity.setEthTransactionId(ethTransactionId);
        transferEntity.setContractEntity(contractEntity);

        // Get amount from transaction
        Transaction transaction = eventPayload.getTransaction();

        BigDecimal incomingEthAmount = ethereumWeiService.toEther(Long.decode(transaction.getValue()));
        transferEntity.setEthAmount(incomingEthAmount);

        BigDecimal arkPerEth = exchangeRateService.getRate();
        transferEntity.setEthToArkRate(BigDecimal.ONE.divide(arkPerEth, 8, RoundingMode.HALF_UP));
        
        transferEntity.setEthFlatFee(config.getFlatFee());
        transferEntity.setEthPercentFee(config.getPercentFee());

        BigDecimal percentFee = config.getPercentFee()
                .divide(new BigDecimal("100.00"), 8, BigDecimal.ROUND_HALF_UP);
        BigDecimal ethTotalFeeAmount = incomingEthAmount.multiply(percentFee).add(config.getFlatFee());
        transferEntity.setEthTotalFee(ethTotalFeeAmount);

        BigDecimal ethSendAmount = incomingEthAmount.subtract(ethTotalFeeAmount);
        BigDecimal arkSendAmount = ethSendAmount.multiply(arkPerEth).setScale(8, RoundingMode.HALF_DOWN);

        BigDecimal arkTransactionFee = Constants.ARK_TRANSACTION_FEE;
        if (arkSendAmount.compareTo(arkTransactionFee) <= 0) {
            arkSendAmount = BigDecimal.ZERO;
        }
        transferEntity.setArkSendAmount(arkSendAmount);

        transferRepository.save(transferEntity);
        
        NewEthTransferEvent newEthTransferEvent = new NewEthTransferEvent();
        newEthTransferEvent.setTransferPid(transferEntity.getPid());
        applicationEventPublisher.publishEvent(newEthTransferEvent);
    }
}
