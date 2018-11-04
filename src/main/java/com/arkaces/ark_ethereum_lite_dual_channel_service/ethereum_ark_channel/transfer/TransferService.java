package com.arkaces.ark_ethereum_lite_dual_channel_service.ethereum_ark_channel.transfer;

import com.arkaces.aces_server.aces_service.notification.NotificationService;
import com.arkaces.ark_ethereum_lite_dual_channel_service.Constants;
import com.arkaces.ark_ethereum_lite_dual_channel_service.config.ServiceBitcoinAccountSettings;
import com.arkaces.ark_ethereum_lite_dual_channel_service.ark.ArkService;
import com.arkaces.ark_ethereum_lite_dual_channel_service.ethereum_ark_channel.contract.ContractEntity;
import com.arkaces.ark_ethereum_lite_dual_channel_service.electrum.ElectrumService;
import com.arkaces.ark_ethereum_lite_dual_channel_service.ethereum_ark_channel.service_capacity.ServiceCapacityEntity;
import com.arkaces.ark_ethereum_lite_dual_channel_service.ethereum_ark_channel.service_capacity.ServiceCapacityRepository;
import com.arkaces.ark_ethereum_lite_dual_channel_service.ethereum_ark_channel.service_capacity.ServiceCapacityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
@Transactional
public class TransferService {

    private final TransferRepository transferRepository;
    private final ArkService arkService;
    private final ElectrumService electrumService;
    private final ServiceCapacityService serviceCapacityService;
    private final ServiceCapacityRepository serviceCapacityRepository;
    private final ServiceBitcoinAccountSettings serviceBitcoinAccountSettings;
    private final NotificationService notificationService;
    private final BigDecimal lowCapacityThreshold;

    /**
     * @return true if amount reserved successfully
     */
    public boolean reserveTransferCapacity(Long transferPid) {
        // Lock service capacity and update available balance if available
        ServiceCapacityEntity serviceCapacityEntity = serviceCapacityService.getLockedCapacityEntity();

        TransferEntity transferEntity = transferRepository.findOneForUpdate(transferPid);

        BigDecimal btcTransactionFeeAmount = new BigDecimal(org.bitcoinj.core.Transaction.DEFAULT_TX_FEE.longValue())
                .divide(new BigDecimal(ElectrumService.SATOSHIS_PER_BTC), 10, BigDecimal.ROUND_HALF_UP);
        BigDecimal totalAmount = transferEntity.getBtcSendAmount().add(btcTransactionFeeAmount);
        BigDecimal newAvailableAmount = serviceCapacityEntity.getAvailableAmount().subtract(totalAmount);
        BigDecimal newUnsettledAmount = serviceCapacityEntity.getUnsettledAmount().add(totalAmount);
        if (newAvailableAmount.compareTo(BigDecimal.ZERO) < 0) {
            return false;
        }
        
        serviceCapacityEntity.setAvailableAmount(newAvailableAmount);
        serviceCapacityEntity.setUnsettledAmount(newUnsettledAmount);
        serviceCapacityRepository.save(serviceCapacityEntity);

        if (serviceCapacityEntity.getAvailableAmount().compareTo(lowCapacityThreshold) <= 0) {
            notificationService.notifyLowCapacity(serviceCapacityEntity.getAvailableAmount(), serviceCapacityEntity.getUnit());
        }
        
        return true;
    }
    
    public void settleTransferCapacity(Long transferPid) {
        ServiceCapacityEntity serviceCapacityEntity = serviceCapacityService.getLockedCapacityEntity();

        TransferEntity transferEntity = transferRepository.findById(transferPid)
                .orElseThrow(() -> new RuntimeException("Failed to get transfer with id " + transferPid));

        BigDecimal btcTransactionFeeAmount = new BigDecimal(org.bitcoinj.core.Transaction.DEFAULT_TX_FEE.longValue())
                .divide(new BigDecimal(ElectrumService.SATOSHIS_PER_BTC), 10, BigDecimal.ROUND_HALF_UP);
        BigDecimal totalAmount = transferEntity.getBtcSendAmount().add(btcTransactionFeeAmount);

        serviceCapacityEntity.setUnsettledAmount(serviceCapacityEntity.getUnsettledAmount().subtract(totalAmount));
        serviceCapacityEntity.setTotalAmount(serviceCapacityEntity.getTotalAmount().subtract(totalAmount));

        serviceCapacityRepository.save(serviceCapacityEntity);
    }
    
    public void processNewTransfer(Long transferPid) {
        TransferEntity transferEntity = transferRepository.findOneForUpdate(transferPid);
        ContractEntity contractEntity = transferEntity.getContractEntity();

        BigDecimal btcTransactionFeeAmount = new BigDecimal(org.bitcoinj.core.Transaction.DEFAULT_TX_FEE.longValue())
                .divide(new BigDecimal(ElectrumService.SATOSHIS_PER_BTC), 10, BigDecimal.ROUND_HALF_UP);
        BigDecimal totalAmount = transferEntity.getBtcSendAmount().subtract(btcTransactionFeeAmount);
        if (totalAmount.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal btcSendAmount = transferEntity.getBtcSendAmount();
            String recipientBtcAddress = contractEntity.getRecipientBtcAddress();

            String btcTransactionId = electrumService
                    .sendTransaction(recipientBtcAddress, totalAmount, serviceBitcoinAccountSettings.getPrivateKey());
            transferEntity.setBtcTransactionId(btcTransactionId);

            log.info("Sent " + btcSendAmount + " btc to " + contractEntity.getRecipientBtcAddress()
                + ", btc transaction id " + btcTransactionId + ", ark transaction " + transferEntity.getArkTransactionId());
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

        log.info("Insufficient btc to send transfer id = " + transferEntity.getId());

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
                "Insufficient btc to send transfer id = " + transferEntity.getId()
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
