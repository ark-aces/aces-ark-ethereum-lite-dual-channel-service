package com.arkaces.ark_ethereum_lite_dual_channel_service.ark_ethereum_channel.service_capacity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service("arkEthereumChannel.serviceCapacityUpdater")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
@ConditionalOnProperty("arkEthereumChannel.enabled")
public class ServiceCapacityUpdater {

    @Qualifier("arkEthereumChannel.serviceCapacityService")
    private final ServiceCapacityService serviceCapacityService;

    @Scheduled(fixedDelayString = "${arkEthereumChannel.capacityUpdateIntervalSec}000")
    public void sweep() {
        try {
            log.info("Updating arkEthereumChannel capacity");
            serviceCapacityService.updateCapacities();
        } catch (Exception e) {
            log.error("Failed to update arkEthereumChannel capacity", e);
        }
    }

}