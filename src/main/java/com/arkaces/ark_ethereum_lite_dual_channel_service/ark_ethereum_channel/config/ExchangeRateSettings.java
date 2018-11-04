package com.arkaces.ark_ethereum_lite_dual_channel_service.ark_ethereum_channel.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Data
@Component("arkEthereumChannel.exchangeRateSettings")
@ConfigurationProperties(prefix = "ark-ethereum-channel.exchange-rate")
public class ExchangeRateSettings {
    private String fromSymbol;
    private String toSymbol;
    private BigDecimal multiplier;
    private BigDecimal fixedRate;
}
