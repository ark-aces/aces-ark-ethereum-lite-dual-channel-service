package com.arkaces.ark_ethereum_lite_dual_channel_service.ark_ethereum_channel.transfer;

import ark_java_client.ArkClient;
import ark_java_client.Transaction;
import com.arkaces.ark_ethereum_lite_dual_channel_service.ark_ethereum_channel.config.Config;
import com.arkaces.ark_ethereum_lite_dual_channel_service.ark_ethereum_channel.contract.ContractEntity;
import com.arkaces.ark_ethereum_lite_dual_channel_service.ark_ethereum_channel.contract.ContractRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ArkListener {

    @Qualifier("arkEthereumChannel.contractRepository")
    private final ContractRepository contractRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final ArkClient arkClient;
    @Qualifier("arkEthereumChannel.config")
    private final Config config;

    @Scheduled(fixedDelayString = "${arkEthereumChannel.arkScanIntervalSec}000")
    @Transactional
    public void scan() {
        log.info("Scanning for Ark transactions");
        try {
            Integer limit = 50;
            Map<String, Transaction> transactionsById = new HashMap<>();
            for (Integer offset = 0; offset < config.getArkScanDepth(); offset += limit) {
                arkClient.getTransactions(limit, offset)
                        .forEach(transaction -> transactionsById.put(transaction.getId(), transaction));
            }

            List<ContractEntity> contractEntities = contractRepository.findAll();
            for (ContractEntity contractEntity : contractEntities) {
                Set<String> existingTxnIds = contractEntity.getTransferEntities().stream()
                        .map(TransferEntity::getArkTransactionId)
                        .collect(Collectors.toSet());

                Set<String> newTxnIds = transactionsById.values().stream()
                        .filter(transaction -> transaction.getRecipientId().equals(contractEntity.getDepositArkAddress()))
                        .filter(transaction -> ! existingTxnIds.contains(transaction.getId()))
                        .filter(transaction -> transaction.getConfirmations() >= config.getArkMinConfirmations())
                        .map(Transaction::getId)
                        .collect(Collectors.toSet());

                for (String txnId : newTxnIds) {
                    NewArkTransactionEvent newArkTransactionEvent = new NewArkTransactionEvent();
                    newArkTransactionEvent.setContractPid(contractEntity.getPid());
                    newArkTransactionEvent.setTransactionId(txnId);
                    newArkTransactionEvent.setTransaction(transactionsById.get(txnId));
                    applicationEventPublisher.publishEvent(newArkTransactionEvent);
                }
            }
        }
        catch (Exception e) {
            log.error("Ark Transaction listener threw exception while running", e);
        }
    }
}
