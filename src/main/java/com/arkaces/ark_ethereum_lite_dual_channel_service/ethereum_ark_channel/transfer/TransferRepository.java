package com.arkaces.ark_ethereum_lite_dual_channel_service.ethereum_ark_channel.transfer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.LockModeType;

@Transactional
public interface TransferRepository extends JpaRepository<TransferEntity, Long> {

    TransferEntity findOneByPid(Long pid);

    TransferEntity findOneByArkTransactionId(String arkTransactionId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from TransferEntity t where t.pid = :pid")
    TransferEntity findOneForUpdate(@Param("pid") Long pid);
}
