package com.arkaces.ark_ethereum_lite_dual_channel_service.ethereum;

import lombok.Data;

import java.util.List;

@Data
public class EthereumRpcRequest {

    private String jsonrpc = "2.0";
    private String method;
    private List<Object> params;
    private Integer id = 1;
}
