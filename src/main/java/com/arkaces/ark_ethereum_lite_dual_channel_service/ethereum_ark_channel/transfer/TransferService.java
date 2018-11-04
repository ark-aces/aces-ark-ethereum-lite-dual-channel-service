package com.arkaces.ark_ethereum_lite_dual_channel_service.ethereum_ark_channel.transfer;

import com.arkaces.aces_server.aces_service.notification.NotificationService;
import com.arkaces.ark_ethereum_lite_dual_channel_service.Constants;
import com.arkaces.ark_ethereum_lite_dual_channel_service.ark.ArkService;
import com.arkaces.ark_ethereum_lite_dual_channel_service.config.ServiceArkAccountSettings;
import com.arkaces.ark_ethereum_lite_dual_channel_service.ethereum_ark_channel.config.Config;
import com.arkaces.ark_ethereum_lite_dual_channel_service.ethereum_ark_channel.contract.ContractEntity;
import com.arkaces.ark_ethereum_lite_dual_channel_service.ethereum_ark_channel.service_capacity.ServiceCapacityEntity;
import com.arkaces.ark_ethereum_lite_dual_channel_service.ethereum_ark_channel.service_capacity.ServiceCapacityRepository;
import com.arkaces.ark_ethereum_lite_dual_channel_service.ethereum_ark_channel.service_capacity.ServiceCapacityService;
import com.arkaces.ark_ethereum_lite_dual_channel_service.config.ServiceEthereumAccountSettings;
import com.arkaces.ark_ethereum_lite_dual_channel_service.ethereum.EthereumService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service("ethereumArkChannel.transferService")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
@Transactional
public class TransferService {

    @Qualifier("ethereumArkChannel.transferRepository")
    private final TransferRepository transferRepository;
    private final ArkService arkService;
    @Qualifier("ethereumArkChannel.serviceCapacityService")
    private final ServiceCapacityService serviceCapacityService;
    @Qualifier("ethereumArkChannel.serviceCapacityRepository")
    private final ServiceCapacityRepository serviceCapacityRepository;
    private final ServiceArkAccountSettings serviceArkAccountSettings;
    private final ServiceEthereumAccountSettings serviceEthereumAccountSettings;
    private final NotificationService notificationService;
    private final EthereumService ethereumService;
    @Qualifier("ethereumArkChannel.config")
    private final Config config;

    /**
     * @return true if amount reserved successfully
     */
    public boolean reserveTransferCapacity(Long transferPid) {
        // Lock service capacity and update available balance if available
        ServiceCapacityEntity serviceCapacityEntity = serviceCapacityService.getLockedCapacityEntity();

        TransferEntity transferEntity = transferRepository.findOneForUpdate(transferPid);

        BigDecimal arkTransactionFeeAmount = Constants.ARK_TRANSACTION_FEE;
        BigDecimal totalAmount = transferEntity.getArkSendAmount().add(arkTransactionFeeAmount);
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

        BigDecimal arkTransactionFeeAmount = Constants.ARK_TRANSACTION_FEE;
        BigDecimal totalAmount = transferEntity.getArkSendAmount().add(arkTransactionFeeAmount);

        serviceCapacityEntity.setUnsettledAmount(serviceCapacityEntity.getUnsettledAmount().subtract(totalAmount));
        serviceCapacityEntity.setTotalAmount(serviceCapacityEntity.getTotalAmount().subtract(totalAmount));

        serviceCapacityRepository.save(serviceCapacityEntity);
    }

    public void processNewTransfer(Long transferPid) {
        TransferEntity transferEntity = transferRepository.findOneForUpdate(transferPid);
        ContractEntity contractEntity = transferEntity.getContractEntity();

        BigDecimal arkSendAmount = transferEntity.getArkSendAmount();
        if (arkSendAmount.compareTo(BigDecimal.ZERO) > 0) {
            String recipientArkAddress = contractEntity.getRecipientArkAddress();

            String arkTransactionId = arkService.sendTransaction(
                    recipientArkAddress,
                    arkSendAmount
            );
            transferEntity.setArkTransactionId(arkTransactionId);

            log.info("Sent " + arkSendAmount + " ark to " + contractEntity.getRecipientArkAddress()
                + ", ark transaction id " + arkTransactionId + ", eth transaction " + transferEntity.getEthTransactionId());
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

        log.info("Insufficient ark to send transfer id = " + transferEntity.getId());

        String returnEthAddress = transferEntity.getContractEntity().getReturnEthAddress();
        if (returnEthAddress != null) {
            BigDecimal returnEthAmount = transferEntity.getEthAmount().subtract(ethereumService.getTransactionFee());
            String returnEthTransactionId = ethereumService.sendTransaction(
                    serviceEthereumAccountSettings.getAddress(),
                    returnEthAddress,
                    returnEthAmount,
                    serviceEthereumAccountSettings.getPassphrase()
            );
            transferEntity.setStatus(TransferStatus.RETURNED);
            transferEntity.setReturnEthTransactionId(returnEthTransactionId);
        } else {
            log.warn("Eth return could not be processed for transfer " + transferPid);
            transferEntity.setStatus(TransferStatus.FAILED);
        }

        transferRepository.save(transferEntity);

        notificationService.notifyFailedTransfer(
                transferEntity.getContractEntity().getId(),
                transferEntity.getId(),
                "Insufficient ark to send transfer id = " + transferEntity.getId()
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
