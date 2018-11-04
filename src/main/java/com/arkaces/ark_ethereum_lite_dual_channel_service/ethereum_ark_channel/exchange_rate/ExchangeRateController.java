package com.arkaces.ark_ethereum_lite_dual_channel_service.ethereum_ark_channel.exchange_rate;

import com.arkaces.ark_ethereum_lite_dual_channel_service.ethereum_ark_channel.config.ExchangeRateSettings;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ExchangeRateController {

    private final ExchangeRateSettings exchangeRateSettings;
    private final ExchangeRateService exchangeRateService;

    @GetMapping("/exchangeRate")
    public ExchangeRate getExchangeRate() {
        ExchangeRate exchangeRate = new ExchangeRate();
        exchangeRate.setFrom(exchangeRateSettings.getFromSymbol());
        exchangeRate.setTo(exchangeRateSettings.getToSymbol());
        exchangeRate.setRate(exchangeRateService.getRate());

        return exchangeRate;
    }
}
