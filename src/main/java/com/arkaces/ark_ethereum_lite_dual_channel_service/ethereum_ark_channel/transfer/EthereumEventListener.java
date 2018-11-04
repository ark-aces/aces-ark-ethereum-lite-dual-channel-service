package com.arkaces.ark_ethereum_lite_dual_channel_service.ethereum_ark_channel.transfer;

import com.arkaces.ark_ethereum_lite_dual_channel_service.ark_ethereum_channel.transfer.TransferEntity;
import com.arkaces.ark_ethereum_lite_dual_channel_service.ethereum_ark_channel.config.Config;
import com.arkaces.ark_ethereum_lite_dual_channel_service.ethereum_ark_channel.contract.ContractEntity;
import com.arkaces.ark_ethereum_lite_dual_channel_service.ethereum_ark_channel.contract.ContractRepository;
import com.arkaces.ark_ethereum_lite_dual_channel_service.ethereum.Block;
import com.arkaces.ark_ethereum_lite_dual_channel_service.ethereum.EthereumService;
import com.arkaces.ark_ethereum_lite_dual_channel_service.ethereum.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpServerErrorException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class EthereumEventListener {

    @Qualifier("ethereumArkChannel.contractRepository")
    private final ContractRepository contractRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final EthereumService ethereumService;
    @Qualifier("ethereumArkChannel.config")
    private final Config config;

    @Scheduled(fixedDelayString = "${ethereumArkChannel.scanIntervalSec}000")
    @Transactional
    public void scanTransactions() {
        try {
            log.info("Scanning for transactions " + LocalDateTime.now().toString());

            // Get first block (latest block)
            Block latestBlock = ethereumService.getLatestBlock();

            log.info("last block " + latestBlock);

            Integer latestBlockNumber = Integer.decode(latestBlock.getNumber());

            log.info("last block number: " + latestBlockNumber);

            // Iterate through blocks using parent hash of last block
            Block lastBlock = latestBlock;
            Map<String, Transaction> transactionsById = new HashMap<>();
            for (int i = 1; i <= config.getMaxScanBlockDepth(); i++) {
                log.info("Scan depth " + i);
                Block block = ethereumService.getBlockByHash(lastBlock.getParentHash());
                if (block == null) {
                    continue;
                }
                for (Transaction transaction : block.getTransactions()) {
                    String transactionId = transaction.getHash();
                    transactionsById.put(transactionId, transaction);
                }
                lastBlock = block;
            }

            List<ContractEntity> contractEntities = contractRepository.findAll();
            for (ContractEntity contractEntity : contractEntities) {
                Set<String> existingTxnIds = contractEntity.getTransferEntities().stream()
                        .map(TransferEntity::getEthTransactionId)
                        .collect(Collectors.toSet());

                Set<String> newTxnIds = transactionsById.values().stream()
                        .filter(transaction -> ! existingTxnIds.contains(transaction.getHash()))
                        .filter(transaction -> transaction.getTo().equals(contractEntity.getDepositEthAddress()))
                        .filter(transaction -> {
                            Integer confirmations = latestBlockNumber - Integer.decode(transaction.getBlockNumber());
                            return confirmations >= config.getMinConfirmations();
                        })
                        .map(Transaction::getHash)
                        .collect(Collectors.toSet());

                for (String txnId : newTxnIds) {
                    Transaction transaction = transactionsById.get(txnId);
                    log.info("saving transaction: " + transaction);

                    NewEthTransactionEvent newEthTransactionEvent = new NewEthTransactionEvent();
                    newEthTransactionEvent.setContractPid(contractEntity.getPid());
                    newEthTransactionEvent.setTransactionId(txnId);
                    newEthTransactionEvent.setTransaction(transactionsById.get(txnId));
                    applicationEventPublisher.publishEvent(newEthTransactionEvent);
                }
            }
        }
        catch (HttpServerErrorException e) {
            log.error("Failed to get transaction data: " + e.getResponseBodyAsString());
        }
        catch (Exception e) {
            log.error("Transaction listener threw exception while running", e);
        }
    }

}
