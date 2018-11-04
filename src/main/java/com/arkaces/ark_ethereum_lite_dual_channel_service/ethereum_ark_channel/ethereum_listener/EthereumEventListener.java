package com.arkaces.ark_ethereum_lite_dual_channel_service.ethereum_ark_channel.ethereum_listener;

import com.arkaces.ark_ethereum_lite_dual_channel_service.ethereum_rpc.Block;
import com.arkaces.ark_ethereum_lite_dual_channel_service.ethereum_rpc.EthereumRpcClient;
import com.arkaces.ark_ethereum_lite_dual_channel_service.ethereum_rpc.Transaction;
import com.arkaces.aces_server.aces_listener.event_delivery.EventDeliveryService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class EthereumEventListener {

    private final Integer maxScanBlockDepth;
    private final EventDeliveryService eventDeliveryService;
    private final EthereumRpcClient ethereumRpcClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Scheduled(fixedDelay = 1000)
    public void scanTransactions() {
        try {
            log.info("Scanning for transactions " + LocalDateTime.now().toString());

            // Get first block (latest block)
            Block latestBlock = ethereumRpcClient.getLatestBlock();

            log.info("last block " + latestBlock);

            Integer latestBlockNumber = Integer.decode(latestBlock.getNumber());

            log.info("last block number: " + latestBlockNumber);

            // Iterate through blocks using parent hash of last block
            Block lastBlock = latestBlock;
            for (int i = 1; i <= maxScanBlockDepth; i++) {
                log.info("Scan depth " + i);
                Block block = ethereumRpcClient.getBlockByHash(lastBlock.getParentHash());
                if (block == null) {
                    continue;
                }
                for (Transaction transaction : block.getTransactions()) {
                    String transactionId = transaction.getHash();
                    String recipientAddress = transaction.getTo();

                    Integer blockNumber = Integer.decode(transaction.getBlockNumber());
                    Integer confirmations = latestBlockNumber - blockNumber;

                    log.info("saving transaction: " + transaction);

                    JsonNode transactionJsonNode = objectMapper.convertValue(transaction, JsonNode.class);

                    eventDeliveryService.saveSubscriptionEvents(
                            transactionId,
                            recipientAddress,
                            confirmations,
                            transactionJsonNode
                    );
                }
                lastBlock = block;
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
