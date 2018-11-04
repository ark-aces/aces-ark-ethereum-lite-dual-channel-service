package com.arkaces.ark_ethereum_lite_dual_channel_service.ethereum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendTransaction {

    private String from;
    private String to;
    private String value;
}
