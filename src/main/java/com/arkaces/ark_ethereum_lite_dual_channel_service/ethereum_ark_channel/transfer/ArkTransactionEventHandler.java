package com.arkaces.ark_ethereum_lite_dual_channel_service.ethereum_ark_channel.transfer;

import ark_java_client.Transaction;
import com.arkaces.aces_server.common.identifer.IdentifierGenerator;
import com.arkaces.ark_ethereum_lite_dual_channel_service.config.Config;
import com.arkaces.ark_ethereum_lite_dual_channel_service.ark.ArkSatoshiService;
import com.arkaces.ark_ethereum_lite_dual_channel_service.ethereum_ark_channel.contract.ContractEntity;
import com.arkaces.ark_ethereum_lite_dual_channel_service.ethereum_ark_channel.contract.ContractRepository;
import com.arkaces.ark_ethereum_lite_dual_channel_service.electrum.ElectrumService;
import com.arkaces.ark_ethereum_lite_dual_channel_service.ethereum_ark_channel.exchange_rate.ExchangeRateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    private final ContractRepository contractRepository;
    private final TransferRepository transferRepository;
    private final IdentifierGenerator identifierGenerator;
    private final ExchangeRateService exchangeRateService;
    private final Config config;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final ArkSatoshiService arkSatoshiService;

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
        
        log.info("Matched event for contract id " + contractEntity.getId() + " btc transaction id " + arkTransactionId);

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

        BigDecimal arkToBtcRate = exchangeRateService.getRate();
        transferEntity.setArkToBtcRate(arkToBtcRate);
        
        transferEntity.setArkFlatFee(config.getFlatFee());
        transferEntity.setArkPercentFee(config.getPercentFee());

        BigDecimal percentFee = config.getPercentFee()
                .divide(new BigDecimal("100.00"), 8, BigDecimal.ROUND_HALF_UP);
        BigDecimal arkTotalFeeAmount = incomingArkAmount.multiply(percentFee).add(config.getFlatFee());
        transferEntity.setArkTotalFee(arkTotalFeeAmount);

        // Calculate send btc amount
        BigDecimal arkSendAmount = incomingArkAmount.subtract(arkTotalFeeAmount);
        BigDecimal btcSendAmount = arkSendAmount.multiply(arkToBtcRate).setScale(8, RoundingMode.HALF_DOWN);

        BigDecimal btcTransactionFeeAmount = new BigDecimal(org.bitcoinj.core.Transaction.DEFAULT_TX_FEE.longValue())
                .divide(new BigDecimal(ElectrumService.SATOSHIS_PER_BTC), 10, BigDecimal.ROUND_HALF_UP);
        if (btcSendAmount.compareTo(btcTransactionFeeAmount) <= 0) {
            btcSendAmount = BigDecimal.ZERO;
        }
        transferEntity.setBtcSendAmount(btcSendAmount);

        transferRepository.save(transferEntity);
        
        NewTransferEvent newTransferEvent = new NewTransferEvent();
        newTransferEvent.setTransferPid(transferEntity.getPid());
        applicationEventPublisher.publishEvent(newTransferEvent);
    }
}
