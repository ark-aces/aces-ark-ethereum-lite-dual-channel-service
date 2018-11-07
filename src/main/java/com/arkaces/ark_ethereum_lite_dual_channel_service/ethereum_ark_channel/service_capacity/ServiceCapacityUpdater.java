package com.arkaces.ark_ethereum_lite_dual_channel_service.ethereum_ark_channel.service_capacity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service("ethereumArkChannel.serviceCapacityUpdater")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
@ConditionalOnProperty("ethereumArkChannel.enabled")
public class ServiceCapacityUpdater {

    @Qualifier("ethereumArkChannel.serviceCapacityService")
    private final ServiceCapacityService serviceCapacityService;

    @Scheduled(fixedDelayString = "${ethereumArkChannel.capacityUpdateIntervalSec}000")
    public void sweep() {
        try {
            log.info("Updating ethereumArkChannel capacity");
            serviceCapacityService.updateCapacities();
        } catch (Exception e) {
            log.error("Failed to update arkEthereumChannel capacity", e);
        }
    }

}