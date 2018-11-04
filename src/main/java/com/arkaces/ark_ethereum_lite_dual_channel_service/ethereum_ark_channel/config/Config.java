package com.arkaces.ark_ethereum_lite_dual_channel_service.ethereum_ark_channel.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Data
@Component("ethereumArkChannel.config")
@ConfigurationProperties(prefix = "ethereum-ark-channel")
public class Config {
    private String capacityUnit;

    private BigDecimal flatFee;
    private String flatFeeUnit;
    private BigDecimal percentFee;

    private Integer maxScanBlockDepth;
    private Integer minConfirmations;

    private BigDecimal lowCapacityThreshold;
}
