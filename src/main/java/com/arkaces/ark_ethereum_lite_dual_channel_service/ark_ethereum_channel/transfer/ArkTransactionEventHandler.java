package com.arkaces.ark_ethereum_lite_dual_channel_service.ark_ethereum_channel.transfer;

import ark_java_client.Transaction;
import com.arkaces.aces_server.common.identifer.IdentifierGenerator;
import com.arkaces.ark_ethereum_lite_dual_channel_service.ark.ArkSatoshiService;
import com.arkaces.ark_ethereum_lite_dual_channel_service.ark_ethereum_channel.config.Config;
import com.arkaces.ark_ethereum_lite_dual_channel_service.ark_ethereum_channel.contract.ContractEntity;
import com.arkaces.ark_ethereum_lite_dual_channel_service.ark_ethereum_channel.contract.ContractRepository;
import com.arkaces.ark_ethereum_lite_dual_channel_service.ark_ethereum_channel.exchange_rate.ExchangeRateService;
import com.arkaces.ark_ethereum_lite_dual_channel_service.ethereum.EthereumService;
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
public class ArkTransactionEventHandler {

    @Qualifier("arkEthereumChannel.contractRepository")
    private final ContractRepository contractRepository;
    @Qualifier("arkEthereumChannel.transferRepository")
    private final TransferRepository transferRepository;
    private final IdentifierGenerator identifierGenerator;
    @Qualifier("arkEthereumChannel.exchangeRateService")
    private final ExchangeRateService exchangeRateService;
    @Qualifier("arkEthereumChannel.config")
    private final Config config;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final ArkSatoshiService arkSatoshiService;
    private final EthereumService ethereumService;

    @EventListener
    @Transactional
    public void handleArkTransactionEvent(NewArkTransactionEvent eventPayload) {
        String arkTransactionId = eventPayload.getTransactionId();
        
        log.info("Received Ark transaction event: " + arkTransactionId + " -> " + eventPayload.getTransaction());
        
        ContractEntity contractEntity = contractRepository.findById(eventPayload.getContractPid()).orElse(null);
        if (contractEntity == null) {
            log.info("Ark event has no corresponding contract: " + eventPayload);
            return;
        }
        
        log.info("Matched event for contract id " + contractEntity.getId() + " ark transaction id " + arkTransactionId);

        TransferEntity existingTransferEntity = transferRepository.findOneByArkTransactionId(arkTransactionId);
        if (existingTransferEntity != null) {
            log.info("Transfer for ark transaction " + arkTransactionId + " already exists with id " + existingTransferEntity.getId());
            return;
        } 
        
        String transferId = identifierGenerator.generate();

        TransferEntity transferEntity = new TransferEntity();
        transferEntity.setId(transferId);
        transferEntity.setStatus(TransferStatus.NEW);
        transferEntity.setCreatedAt(LocalDateTime.now());
        transferEntity.setArkTransactionId(arkTransactionId);
        transferEntity.setContractEntity(contractEntity);

        // Get amount from transaction
        Transaction transaction = eventPayload.getTransaction();

        BigDecimal incomingArkAmount = arkSatoshiService.toArk(transaction.getAmount());
        transferEntity.setArkAmount(incomingArkAmount);

        BigDecimal ethPerArk = exchangeRateService.getRate();
        transferEntity.setArkToEthRate(BigDecimal.ONE.divide(ethPerArk, 8, RoundingMode.HALF_UP));
        
        transferEntity.setArkFlatFee(config.getFlatFee());
        transferEntity.setArkPercentFee(config.getPercentFee());

        BigDecimal percentFee = config.getPercentFee()
                .divide(new BigDecimal("100.00"), 8, BigDecimal.ROUND_HALF_UP);
        BigDecimal arkTotalFeeAmount = incomingArkAmount.multiply(percentFee).add(config.getFlatFee());
        transferEntity.setArkTotalFee(arkTotalFeeAmount);

        BigDecimal arkSendAmount = incomingArkAmount.subtract(arkTotalFeeAmount);
        BigDecimal ethSendAmount = arkSendAmount.multiply(ethPerArk).setScale(8, RoundingMode.HALF_DOWN);

        BigDecimal ethTransactionFee = ethereumService.getTransactionFee();
        if (ethSendAmount.compareTo(ethTransactionFee) <= 0) {
            ethSendAmount = BigDecimal.ZERO;
        }
        transferEntity.setEthSendAmount(ethSendAmount);

        transferRepository.save(transferEntity);
        
        NewArkTransferEvent newArkTransferEvent = new NewArkTransferEvent();
        newArkTransferEvent.setTransferPid(transferEntity.getPid());
        applicationEventPublisher.publishEvent(newArkTransferEvent);
    }
}
