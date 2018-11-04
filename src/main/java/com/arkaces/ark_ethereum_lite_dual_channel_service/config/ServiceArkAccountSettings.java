package com.arkaces.ark_ethereum_lite_dual_channel_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "service-ark-account")
public class ServiceArkAccountSettings {
    private String address;
    private String passphrase;
}
