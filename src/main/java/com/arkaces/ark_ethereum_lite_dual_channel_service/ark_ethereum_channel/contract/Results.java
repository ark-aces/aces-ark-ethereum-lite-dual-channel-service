package com.arkaces.ark_ethereum_lite_dual_channel_service.ark_ethereum_channel.contract;

import com.arkaces.ark_ethereum_lite_dual_channel_service.ark_ethereum_channel.transfer.Transfer;
import lombok.Data;

import java.util.List;

@Data
public class Results {
    private String recipientEthAddress;
    private String returnArkAddress;
    private String depositArkAddress;
    private List<Transfer> transfers;
}