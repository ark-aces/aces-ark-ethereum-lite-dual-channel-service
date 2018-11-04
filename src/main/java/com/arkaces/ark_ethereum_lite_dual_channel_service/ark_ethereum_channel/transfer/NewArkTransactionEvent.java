package com.arkaces.ark_ethereum_lite_dual_channel_service.ark_ethereum_channel.transfer;

import ark_java_client.Transaction;
import lombok.Data;

@Data
public class NewArkTransactionEvent {
    private Long contractPid;
    private String transactionId;
    private Transaction transaction;
}

