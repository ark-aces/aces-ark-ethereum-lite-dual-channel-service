package com.arkaces.ark_ethereum_lite_dual_channel_service.ethereum_ark_channel.transfer;

import org.springframework.stereotype.Service;

import java.time.ZoneOffset;

@Service("ethereumArkChannel.transferMapper")
public class TransferMapper {
    
    public Transfer map(TransferEntity transferEntity) {
        Transfer transfer = new Transfer();
        transfer.setId(transferEntity.getId());
        transfer.setStatus(transferEntity.getStatus());
        transfer.setEthTransactionId(transferEntity.getEthTransactionId());
        transfer.setArkSendAmount(transferEntity.getArkSendAmount().toPlainString());
        transfer.setArkTransactionId(transferEntity.getArkTransactionId());
        transfer.setEthAmount(transferEntity.getEthAmount().toPlainString());
        transfer.setEthFlatFee(transferEntity.getEthFlatFee().toPlainString());
        transfer.setEthPercentFee(transferEntity.getEthPercentFee().toPlainString());
        transfer.setEthToArkRate(transferEntity.getEthToArkRate().toPlainString());
        transfer.setEthTotalFee(transferEntity.getEthTotalFee().toPlainString());
        transfer.setCreatedAt(transferEntity.getCreatedAt().atOffset(ZoneOffset.UTC).toString());
        transfer.setReturnEthTransactionId(transferEntity.getReturnEthTransactionId());
        
        return transfer;
    }
    
}
