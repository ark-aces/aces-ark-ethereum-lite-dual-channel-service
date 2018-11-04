package com.arkaces.ark_ethereum_lite_dual_channel_service.ark_ethereum_channel.transfer;

import org.springframework.stereotype.Service;

import java.time.ZoneOffset;

@Service("arkEthereumChannel.transferMapper")
public class TransferMapper {
    
    public Transfer map(TransferEntity transferEntity) {
        Transfer transfer = new Transfer();
        transfer.setId(transferEntity.getId());
        transfer.setStatus(transferEntity.getStatus());
        transfer.setArkTransactionId(transferEntity.getArkTransactionId());
        transfer.setEthSendAmount(transferEntity.getEthSendAmount().toPlainString());
        transfer.setEthTransactionId(transferEntity.getEthTransactionId());
        transfer.setArkAmount(transferEntity.getArkAmount().toPlainString());
        transfer.setArkFlatFee(transferEntity.getArkFlatFee().toPlainString());
        transfer.setArkPercentFee(transferEntity.getArkPercentFee().toPlainString());
        transfer.setArkToEthRate(transferEntity.getArkToEthRate().toPlainString());
        transfer.setArkTotalFee(transferEntity.getArkTotalFee().toPlainString());
        transfer.setCreatedAt(transferEntity.getCreatedAt().atOffset(ZoneOffset.UTC).toString());
        transfer.setReturnArkTransactionId(transferEntity.getReturnArkTransactionId());
        
        return transfer;
    }
    
}
