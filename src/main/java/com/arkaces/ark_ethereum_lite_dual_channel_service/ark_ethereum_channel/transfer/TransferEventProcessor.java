package com.arkaces.ark_ethereum_lite_dual_channel_service.ark_ethereum_channel.transfer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class TransferEventProcessor {

    @Qualifier("arkEthereumChannel.transferService")
    private final TransferService transferService;

    @EventListener
    public void handleNewTransferEvent(NewArkTransferEvent newTransferEvent) {
        Long transferPid = newTransferEvent.getTransferPid();
        try {
            if (transferService.reserveTransferCapacity(transferPid)) {
                transferService.processNewTransfer(transferPid);
            } else {
                transferService.processReturn(transferPid);
            }
            transferService.settleTransferCapacity(transferPid);
        } catch (Exception e) {
            log.error("Exception handling new transfer event", e);
            try {
                transferService.processFailedTransfer(transferPid, e.getMessage());
                transferService.settleTransferCapacity(transferPid);
            } catch (Exception e2) {
                log.error("Exception settling failed transfer event", e);
            }
        }
    }
}
