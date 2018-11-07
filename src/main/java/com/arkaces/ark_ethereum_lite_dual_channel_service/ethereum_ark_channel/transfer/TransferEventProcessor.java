package com.arkaces.ark_ethereum_lite_dual_channel_service.ethereum_ark_channel.transfer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component("ethereumArkChannel.transferEventProcessor")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class TransferEventProcessor {

    @Qualifier("ethereumArkChannel.transferService")
    private final TransferService transferService;

    @EventListener
    public void handleNewTransferEvent(NewEthTransferEvent newTransferEvent) {
        Long transferPid = newTransferEvent.getTransferPid();
        if (transferService.reserveTransferCapacity(transferPid)) {
            try {
                transferService.processNewTransfer(transferPid);
            } catch (Exception e) {
                log.error("Exception settling failed transfer event", e);
                transferService.processFailedTransfer(transferPid, e.getMessage());
            }
            transferService.settleTransferCapacity(transferPid);
        } else {
            transferService.processReturn(transferPid);
        }
    }
}
