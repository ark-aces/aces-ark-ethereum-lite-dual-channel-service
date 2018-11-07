package com.arkaces.ark_ethereum_lite_dual_channel_service.ethereum_ark_channel.service_info;

import com.arkaces.aces_server.aces_service.server_info.Capacity;
import com.arkaces.ark_ethereum_lite_dual_channel_service.ark_ethereum_channel.config.Config;
import com.arkaces.ark_ethereum_lite_dual_channel_service.ark_ethereum_channel.config.ServerInfoSettings;
import com.arkaces.ark_ethereum_lite_dual_channel_service.common.server_info.ServerInfo;
import com.arkaces.ark_ethereum_lite_dual_channel_service.ethereum_ark_channel.service_capacity.ServiceCapacityService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

@RestController("ethereumArkChannel.serverInfoController")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@ConditionalOnProperty("ethereumArkChannel.enabled")
@RequestMapping(path = "${ethereumArkChannel.urlPrefix}")
public class ServerInfoController {

    @Qualifier("ethereumArkChannel.serverInfoSettings")
    private final ServerInfoSettings serverInfoSettings;
    private final ObjectMapper objectMapper;
    @Qualifier("ethereumArkChannel.serviceCapacityService")
    private final ServiceCapacityService serviceCapacityService;
    @Qualifier("ethereumArkChannel.config")
    private final Config config;

    @Value("${ethereumArkChannel.urlPrefix}")
    private String urlPrefix;
    
    @GetMapping("")
    public ServerInfo getServerInfo() {
        JsonNode inputSchemaJsonNode;
        try {
            inputSchemaJsonNode = objectMapper.readTree(serverInfoSettings.getInputSchema());
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse inputSchema json", e);
        }

        JsonNode outputSchemaJsonNode;
        try {
            outputSchemaJsonNode = objectMapper.readTree(serverInfoSettings.getOutputSchema());
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse outputSchema json", e);
        }

        ServerInfo serverInfo = new ServerInfo();
        serverInfo.setName(serverInfoSettings.getName());
        serverInfo.setDescription(serverInfoSettings.getDescription());
        serverInfo.setInstructions(serverInfoSettings.getInstructions());
        serverInfo.setVersion(serverInfoSettings.getVersion());
        serverInfo.setWebsiteUrl(serverInfoSettings.getWebsiteUrl());
        serverInfo.setCapacities(serverInfoSettings.getCapacities());

        serverInfo.setFlatFee(config.getFlatFee());
        serverInfo.setFlatFeeUnit(config.getFlatFeeUnit());
        serverInfo.setPercentFee(config.getPercentFee());
        serverInfo.setInputSchema(inputSchemaJsonNode);
        serverInfo.setOutputSchema(outputSchemaJsonNode);
        serverInfo.setInterfaces(Collections.singletonList("transferChannel"));

        serverInfo.setOutputSchemaUrlTemplates(serverInfoSettings.getOutputSchemaUrlTemplates());

        serverInfo.setExchangeRateHref(urlPrefix + "/exchangeRate");

        Capacity capacity = new Capacity();
        capacity.setUnit(config.getCapacityUnit());
        capacity.setValue(serviceCapacityService.getAvailableAmount());
        serverInfo.setCapacities(Arrays.asList(capacity));

        return serverInfo;
    }
}
