package com.arkaces.ark_ethereum_lite_dual_channel_service.ethereum_ark_channel.service_capacity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class ServiceCapacityInitializer implements ApplicationListener<ApplicationReadyEvent> {

    private final ServiceCapacityService serviceCapacityService;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        log.info("Updating service capacity");
        serviceCapacityService.updateCapacities();
    }
}
