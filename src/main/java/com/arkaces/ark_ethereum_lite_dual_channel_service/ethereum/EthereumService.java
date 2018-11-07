package com.arkaces.ark_ethereum_lite_dual_channel_service.ethereum;

import com.arkaces.aces_server.common.json.NiceObjectMapper;
import com.arkaces.ark_ethereum_lite_dual_channel_service.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EthereumService {

    private final EthereumWeiService ethereumWeiService;
    private final NiceObjectMapper objectMapper = new NiceObjectMapper(new ObjectMapper());
    private final EthereumRpcRequestFactory ethereumRpcRequestFactory = new EthereumRpcRequestFactory();
    private final RestTemplate ethereumRpcRestTemplate;

    private final Integer accountUnlockTimeoutSeconds = 30;

    public Block getLatestBlock() {
        HttpEntity<String> requestEntity = getRequestEntity("eth_getBlockByNumber", Arrays.asList("latest", true));
        return ethereumRpcRestTemplate
                .exchange(
                        "/",
                        HttpMethod.POST,
                        requestEntity,
                        new ParameterizedTypeReference<EthereumRpcResponse<Block>>() {}
                )
                .getBody()
                .getResult();
    }

    public Block getBlockByHash(String hash) {
        HttpEntity<String> requestEntity = getRequestEntity("eth_getBlockByHash", Arrays.asList(hash, true));
        return ethereumRpcRestTemplate
                .exchange(
                        "/",
                        HttpMethod.POST,
                        requestEntity,
                        new ParameterizedTypeReference<EthereumRpcResponse<Block>>() {}
                )
                .getBody()
                .getResult();
    }

    public BigDecimal getTransactionFee() {
        return ethereumWeiService.toEther(Constants.GAS_PRICE.multiply(new BigInteger("21000")).longValue());
    }

    public String createAddress(String passphrase) {
        HttpEntity<String> requestEntity = getRequestEntity("personal_newAccount", Arrays.asList(passphrase));
        return ethereumRpcRestTemplate.exchange(
                "/",
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<EthereumRpcResponse<String>>() {}
        ).getBody().getResult();
    }

    public String sendTransaction(String from, String to, BigDecimal ethValue, String fromPassphrase) {
        EthereumRpcResponse<Boolean> unlockResponse = ethereumRpcRestTemplate
                .exchange(
                        "/",
                        HttpMethod.POST,
                        getRequestEntity("personal_unlockAccount", Arrays.asList(
                                from,
                                fromPassphrase,
                                accountUnlockTimeoutSeconds
                        )),
                        new ParameterizedTypeReference<EthereumRpcResponse<Boolean>>() {}
                )
                .getBody();
        if (unlockResponse.getError() != null || unlockResponse.getResult() == null || ! unlockResponse.getResult()) {
            RpcError rpcError = unlockResponse.getError();
            throw new EthereumRpcException("Failed to unlock service account", rpcError.getCode(), rpcError.getMessage());
        }

        Long wei = ethereumWeiService.toWei(ethValue);
        String value = getHexStringFromWei(wei);
        SendTransaction sendTransaction = SendTransaction.builder()
                .from(from)
                .to(to)
                .value(value)
                .build();
        HttpEntity<String> requestEntity = getRequestEntity("eth_sendTransaction", Collections.singletonList(sendTransaction));

        return ethereumRpcRestTemplate
                .exchange(
                        "/",
                        HttpMethod.POST,
                        requestEntity,
                        new ParameterizedTypeReference<EthereumRpcResponse<String>>() {}
                )
                .getBody()
                .getResult();
    }

    public BigDecimal getBalance(String address) {
        HttpEntity<String> requestEntity = getRequestEntity("eth_getBalance", Arrays.asList(address, "latest"));
        EthereumRpcResponse<String> response = ethereumRpcRestTemplate
                .exchange(
                        "/",
                        HttpMethod.POST,
                        requestEntity,
                        new ParameterizedTypeReference<EthereumRpcResponse<String>>() {}
                )
                .getBody();

        if (response.getError() != null) {
            RpcError rpcError = response.getError();
            throw new EthereumRpcException("Failed to get balance", rpcError.getCode(), rpcError.getMessage());
        }

        BigInteger wei = getBigIntegerFromHexString(response.getResult());

        return ethereumWeiService.toEther(wei);
    }

    private BigInteger getBigIntegerFromHexString(String hexString) {
        return new BigInteger(hexString.replaceFirst("0x", ""), 16);
    }

    private String getHexStringFromWei(Long wei) {
        return "0x" + removeLeadingZeros(Long.toHexString(wei));
    }

    private String removeLeadingZeros(String s) {
        int index = findFirstNonZeroIndex(s);
        if (index == -1) {
            return "0";
        }
        return s.substring(index);
    }

    private int findFirstNonZeroIndex(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) != '0') {
                return i;
            }
        }
        return -1;
    }

    private HttpEntity<String> getRequestEntity(String method, List<Object> params) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Content-Type", "application/json");

        EthereumRpcRequest ethereumRpcRequest = ethereumRpcRequestFactory.create(method, params);
        String body = objectMapper.writeValueAsString(ethereumRpcRequest);

        return new HttpEntity<>(body, headers);
    }
}
