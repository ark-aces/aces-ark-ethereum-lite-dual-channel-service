package com.arkaces.ark_ethereum_lite_dual_channel_service.ethereum_ark_channel.transfer;

import lombok.Data;

@Data
public class Transfer {
    private String id;
    private String status;
    private String createdAt;
    private String ethTransactionId;
    private String ethAmount;
    private String ethToArkRate;
    private String ethFlatFee;
    private String ethPercentFee;
    private String ethTotalFee;
    private String arkSendAmount;
    private String arkTransactionId;
    private String returnEthTransactionId;
}
