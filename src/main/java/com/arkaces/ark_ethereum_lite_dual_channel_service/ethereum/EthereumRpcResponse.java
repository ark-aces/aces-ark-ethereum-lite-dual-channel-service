package com.arkaces.ark_ethereum_lite_dual_channel_service.ethereum;

import lombok.Data;

@Data
public class EthereumRpcResponse<T> {

    private Integer id;
    private String jsonrpc;
    private T result;
    private RpcError error;
}
