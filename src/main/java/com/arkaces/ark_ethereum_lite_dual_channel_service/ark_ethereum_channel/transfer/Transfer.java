package com.arkaces.ark_ethereum_lite_dual_channel_service.ark_ethereum_channel.transfer;

import lombok.Data;

@Data
public class Transfer {
    private String id;
    private String status;
    private String createdAt;
    private String arkTransactionId;
    private String arkAmount;
    private String arkToEthRate;
    private String arkFlatFee;
    private String arkPercentFee;
    private String arkTotalFee;
    private String ethSendAmount;
    private String ethTransactionId;
    private String returnArkTransactionId;
}
