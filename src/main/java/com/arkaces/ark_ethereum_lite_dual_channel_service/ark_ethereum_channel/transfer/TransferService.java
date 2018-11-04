package com.arkaces.ark_ethereum_lite_dual_channel_service.ark_ethereum_channel.transfer;

import com.arkaces.aces_server.aces_service.notification.NotificationService;
import com.arkaces.ark_ethereum_lite_dual_channel_service.Constants;
import com.arkaces.ark_ethereum_lite_dual_channel_service.ark_ethereum_channel.config.Config;
import com.arkaces.ark_ethereum_lite_dual_channel_service.config.ServiceEthereumAccountSettings;
import com.arkaces.ark_ethereum_lite_dual_channel_service.ark.ArkService;
import com.arkaces.ark_ethereum_lite_dual_channel_service.ark_ethereum_channel.contract.ContractEntity;
import com.arkaces.ark_ethereum_lite_dual_channel_service.ark_ethereum_channel.service_capacity.ServiceCapacityEntity;
import com.arkaces.ark_ethereum_lite_dual_channel_service.ark_ethereum_channel.service_capacity.ServiceCapacityRepository;
import com.arkaces.ark_ethereum_lite_dual_channel_service.ark_ethereum_channel.service_capacity.ServiceCapacityService;
import com.arkaces.ark_ethereum_lite_dual_channel_service.ethereum.EthereumService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service("arkEthereumChannel.transferService")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
@Transactional
public class TransferService {

    @Qualifier("arkEthereumChannel.transferRepository")
    private final TransferRepository transferRepository;
    private final ArkService arkService;
    @Qualifier("arkEthereumChannel.serviceCapacityService")
    private final ServiceCapacityService serviceCapacityService;
    @Qualifier("arkEthereumChannel.serviceCapacityRepository")
    private final ServiceCapacityRepository serviceCapacityRepository;
    private final ServiceEthereumAccountSettings serviceEthereumAccountSettings;
    private final NotificationService notificationService;
    private final EthereumService ethereumService;
    @Qualifier("arkEthereumChannel.config")
    private final Config config;

    /**
     * @return true if amount reserved successfully
     */
    public boolean reserveTransferCapacity(Long transferPid) {
        // Lock service capacity and update available balance if available
        ServiceCapacityEntity serviceCapacityEntity = serviceCapacityService.getLockedCapacityEntity();

        TransferEntity transferEntity = transferRepository.findOneForUpdate(transferPid);

        BigDecimal ethTransactionFeeAmount = ethereumService.getTransactionFee();
        BigDecimal totalAmount = transferEntity.getEthSendAmount().add(ethTransactionFeeAmount);
        BigDecimal newAvailableAmount = serviceCapacityEntity.getAvailableAmount().subtract(totalAmount);
        BigDecimal newUnsettledAmount = serviceCapacityEntity.getUnsettledAmount().add(totalAmount);
        if (newAvailableAmount.compareTo(BigDecimal.ZERO) < 0) {
            return false;
        }
        
        serviceCapacityEntity.setAvailableAmount(newAvailableAmount);
        serviceCapacityEntity.setUnsettledAmount(newUnsettledAmount);
        serviceCapacityRepository.save(serviceCapacityEntity);

        if (serviceCapacityEntity.getAvailableAmount().compareTo(config.getLowCapacityThreshold()) <= 0) {
            notificationService.notifyLowCapacity(serviceCapacityEntity.getAvailableAmount(), serviceCapacityEntity.getUnit());
        }
        
        return true;
    }

    public void settleTransferCapacity(Long transferPid) {
        ServiceCapacityEntity serviceCapacityEntity = serviceCapacityService.getLockedCapacityEntity();

        TransferEntity transferEntity = transferRepository.findById(transferPid)
                .orElseThrow(() -> new RuntimeException("Failed to get transfer with id " + transferPid));

        BigDecimal ethTransactionFeeAmount = ethereumService.getTransactionFee();
        BigDecimal totalAmount = transferEntity.getEthSendAmount().add(ethTransactionFeeAmount);

        serviceCapacityEntity.setUnsettledAmount(serviceCapacityEntity.getUnsettledAmount().subtract(totalAmount));
        serviceCapacityEntity.setTotalAmount(serviceCapacityEntity.getTotalAmount().subtract(totalAmount));

        serviceCapacityRepository.save(serviceCapacityEntity);
    }
    
    public void processNewTransfer(Long transferPid) {
        TransferEntity transferEntity = transferRepository.findOneForUpdate(transferPid);
        ContractEntity contractEntity = transferEntity.getContractEntity();

        BigDecimal ethSendAmount = transferEntity.getEthSendAmount();
        if (ethSendAmount.compareTo(BigDecimal.ZERO) > 0) {
            String recipientEthAddress = contractEntity.getRecipientEthAddress();

            String ethTransactionId = ethereumService.sendTransaction(
                    serviceEthereumAccountSettings.getAddress(),
                    recipientEthAddress,
                    ethSendAmount,
                    serviceEthereumAccountSettings.getPassphrase()
            );
            transferEntity.setEthTransactionId(ethTransactionId);

            log.info("Sent " + ethSendAmount + " eth to " + contractEntity.getRecipientEthAddress()
                + ", eth transaction id " + ethTransactionId + ", ark transaction " + transferEntity.getArkTransactionId());
        } 
        
        transferEntity.setStatus(TransferStatus.COMPLETE);
        transferRepository.save(transferEntity);

        log.info("Saved transfer id " + transferEntity.getId() + " to contract " + contractEntity.getId());

        notificationService.notifySuccessfulTransfer(
                transferEntity.getContractEntity().getId(),
                transferEntity.getId()
        );
    }

    /**
     * Process a full return due to insufficient capacity
     * @param transferPid
     */
    public void processReturn(Long transferPid) {
        TransferEntity transferEntity = transferRepository.findOneForUpdate(transferPid);

        log.info("Insufficient eth to send transfer id = " + transferEntity.getId());

        String returnArkAddress = transferEntity.getContractEntity().getReturnArkAddress();
        if (returnArkAddress != null) {
            BigDecimal returnArkAmount = transferEntity.getArkAmount().subtract(Constants.ARK_TRANSACTION_FEE);
            String returnArkTransactionId = arkService.sendTransaction(returnArkAddress, returnArkAmount);
            transferEntity.setStatus(TransferStatus.RETURNED);
            transferEntity.setReturnArkTransactionId(returnArkTransactionId);
        } else {
            log.warn("Ark return could not be processed for transfer " + transferPid);
            transferEntity.setStatus(TransferStatus.FAILED);
        }

        transferRepository.save(transferEntity);

        notificationService.notifyFailedTransfer(
                transferEntity.getContractEntity().getId(),
                transferEntity.getId(),
                "Insufficient eth to send transfer id = " + transferEntity.getId()
        );
    }
    
    public void processFailedTransfer(Long transferPid, String reasonMessage) {
        TransferEntity transferEntity = transferRepository.findOneForUpdate(transferPid);
        transferEntity.setStatus(TransferStatus.FAILED);
        transferRepository.save(transferEntity);

        notificationService.notifyFailedTransfer(
                transferEntity.getContractEntity().getId(),
                transferEntity.getId(),
                reasonMessage
        );
    }
    
}
