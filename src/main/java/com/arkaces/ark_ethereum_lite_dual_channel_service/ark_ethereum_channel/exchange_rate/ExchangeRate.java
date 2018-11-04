package com.arkaces.ark_ethereum_lite_dual_channel_service.ark_ethereum_channel.exchange_rate;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ExchangeRate {
    private BigDecimal rate;
    private String from;
    private String to;
}
