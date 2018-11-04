package com.arkaces.ark_ethereum_lite_dual_channel_service.ethereum_ark_channel.transfer;

import lombok.Data;

@Data
public class Transfer {
    private String id;
    private String status;
    private String createdAt;
    private String arkTransactionId;
    private String arkAmount;
    private String arkToBtcRate;
    private String arkFlatFee;
    private String arkPercentFee;
    private String arkTotalFee;
    private String btcSendAmount;
    private String btcTransactionId;
    private String returnArkTransactionId;
}
