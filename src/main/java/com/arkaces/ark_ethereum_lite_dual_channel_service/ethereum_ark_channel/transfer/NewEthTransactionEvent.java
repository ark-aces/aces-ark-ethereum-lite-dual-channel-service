package com.arkaces.ark_ethereum_lite_dual_channel_service.ethereum_ark_channel.transfer;

import com.arkaces.ark_ethereum_lite_dual_channel_service.ethereum.Transaction;
import lombok.Data;

@Data
public class NewEthTransactionEvent {
    private Long contractPid;
    private String transactionId;
    private Transaction transaction;
}

