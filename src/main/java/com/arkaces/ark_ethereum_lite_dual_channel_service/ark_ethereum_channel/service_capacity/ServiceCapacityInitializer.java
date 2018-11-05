package com.arkaces.ark_ethereum_lite_dual_channel_service.ark_ethereum_channel.service_capacity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component("arkEthereumChannel.serviceCapacityInitializer")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
@ConditionalOnProperty("arkEthereumChannel.enabled")
public class ServiceCapacityInitializer implements ApplicationListener<ApplicationReadyEvent> {

    @Qualifier("arkEthereumChannel.serviceCapacityService")
    private final ServiceCapacityService serviceCapacityService;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        log.info("Updating service capacity");
        serviceCapacityService.updateCapacities();
    }
}
