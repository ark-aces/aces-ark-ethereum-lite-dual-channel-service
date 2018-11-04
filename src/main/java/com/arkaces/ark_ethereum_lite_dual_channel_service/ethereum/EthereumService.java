package com.arkaces.ark_ethereum_lite_dual_channel_service.ethereum;

import com.arkaces.aces_server.common.json.NiceObjectMapper;
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

    public void unlockWallet() {
        EthereumRpcResponse<Boolean> unlockResponse = ethereumRpcRestTemplate
                .exchange(
                        "/",
                        HttpMethod.POST,
                        getRequestEntity("personal_unlockAccount", Arrays.asList(
                                serviceEthAccountSettings.getAddress(),
                                serviceEthAccountSettings.getPassphrase(),
                                accountUnlockTimeoutSeconds
                        )),
                        new ParameterizedTypeReference<EthereumRpcResponse<Boolean>>() {}
                )
                .getBody();

        if (unlockResponse.getError() != null || unlockResponse.getResult() == null || ! unlockResponse.getResult()) {
            RpcError rpcError = unlockResponse.getError();
            throw new EthereumRpcException("Failed to unlock service account", rpcError.getCode(), rpcError.getMessage());
        }

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

    public String sendTransaction(String from, String to, BigDecimal etherValue) {
        Long wei = ethereumWeiService.toWei(etherValue);
        String value = getHexStringFromWei(wei);
        SendTransaction sendTransaction = SendTransaction.builder()
                .from(from)
                .to(to)
                .value(value)
                .build();
        HttpEntity<String> requestEntity = getRequestEntity("eth_sendTransaction", Collections.singletonList(sendTransaction));
        return ethereumRpcRestTemplate.exchange(
                "/",
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<EthereumRpcResponse<String>>() {}
        ).getBody().getResult();
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
