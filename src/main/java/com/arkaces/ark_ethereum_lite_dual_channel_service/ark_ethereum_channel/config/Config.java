package com.arkaces.ark_ethereum_lite_dual_channel_service.ark_ethereum_channel.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Data
@Component("arkEthereumChannel.config")
@ConfigurationProperties(prefix = "ark-ethereum-channel")
public class Config {
    private String capacityUnit;

    private BigDecimal flatFee;
    private String flatFeeUnit;
    private BigDecimal percentFee;

    private Integer arkScanDepth;
    private Integer arkMinConfirmations;

    private BigDecimal lowCapacityThreshold;
}
