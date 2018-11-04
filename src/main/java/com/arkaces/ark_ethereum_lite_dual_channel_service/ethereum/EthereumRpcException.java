package com.arkaces.ark_ethereum_lite_dual_channel_service.ethereum;

public class EthereumRpcException extends RuntimeException {

    private final Integer ethErrorCode;
    private final String ethErrorMessage;

    public EthereumRpcException(String message, Integer ethErrorCode, String ethErrorMessage) {
        super(message);
        this.ethErrorCode = ethErrorCode;
        this.ethErrorMessage = ethErrorMessage;
    }

    public Integer getEthErrorCode() {
        return ethErrorCode;
    }

    public String getEthErrorMessage() {
        return ethErrorMessage;
    }
}
