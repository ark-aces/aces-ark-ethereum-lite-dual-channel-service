package com.arkaces.ark_ethereum_lite_dual_channel_service.ark_ethereum_channel.service_capacity;

import com.arkaces.ark_ethereum_lite_dual_channel_service.ark.ArkService;
import com.arkaces.ark_ethereum_lite_dual_channel_service.ark_ethereum_channel.config.Config;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

@Slf4j
@Service("arkEthereumChannel.serviceCapacityService")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Transactional
public class ServiceCapacityService {
    
    private final ArkService arkService;
    @Qualifier("arkEthereumChannel.serviceCapacityRepository")
    private final ServiceCapacityRepository serviceCapacityRepository;
    @Qualifier("arkEthereumChannel.config")
    private final Config config;

    public void updateCapacities() {
        LocalDateTime now = LocalDateTime.now();
        
        // Lock service capacity row and lock so that no other threads can change capacity while updating
        ServiceCapacityEntity serviceCapacityEntity = serviceCapacityRepository.findOneForUpdate(1L);

        // Get ark balance, this tries up to 5 times in case nodes are not responding
        SimpleRetryPolicy policy = new SimpleRetryPolicy(5, Collections.singletonMap(Exception.class, true));
        RetryTemplate template = new RetryTemplate();
        template.setRetryPolicy(policy);
        BigDecimal accountBalance;
        try {
            accountBalance = template.execute((RetryCallback<BigDecimal, Exception>) context ->
                    arkService.getServiceArkBalance());
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse value", e);
        }

        if (serviceCapacityEntity == null) {
            log.info("Capacity info does not exist, creating now!");

            serviceCapacityEntity = new ServiceCapacityEntity();
            serviceCapacityEntity.setAvailableAmount(accountBalance);
            serviceCapacityEntity.setUnsettledAmount(BigDecimal.ZERO);
            serviceCapacityEntity.setTotalAmount(accountBalance);
            serviceCapacityEntity.setUnit(config.getCapacityUnit());
            serviceCapacityEntity.setCreatedAt(now);
            serviceCapacityEntity.setUpdatedAt(now);
        } else {
            log.info("Capacity info exists, updating now!");
            
            BigDecimal availableAmount = accountBalance.subtract(serviceCapacityEntity.getUnsettledAmount());
            
            serviceCapacityEntity.setAvailableAmount(availableAmount);
            serviceCapacityEntity.setTotalAmount(accountBalance);
            serviceCapacityEntity.setUnit(config.getCapacityUnit());
            serviceCapacityEntity.setUpdatedAt(now);

        }
        serviceCapacityRepository.save(serviceCapacityEntity);

        log.info("Updated capacity: " + serviceCapacityEntity);
    }
    
    public ServiceCapacityEntity getLockedCapacityEntity() {
        return serviceCapacityRepository.findOneForUpdate(1L);
    }
    
    public BigDecimal getAvailableAmount() {
        return serviceCapacityRepository.findById(1L)
            .map(ServiceCapacityEntity::getAvailableAmount)
            .orElse(BigDecimal.ZERO);
    }
    
}
