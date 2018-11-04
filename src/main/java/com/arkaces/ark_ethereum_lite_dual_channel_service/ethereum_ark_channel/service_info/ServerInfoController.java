package com.arkaces.ark_ethereum_lite_dual_channel_service.ethereum_ark_channel.service_info;

import com.arkaces.aces_server.aces_service.server_info.Capacity;
import com.arkaces.ark_ethereum_lite_dual_channel_service.config.Config;
import com.arkaces.ark_ethereum_lite_dual_channel_service.ethereum_ark_channel.service_capacity.ServiceCapacityService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ServerInfoController {
    
    private final ServerInfoSettings serverInfoSettings;    
    private final ObjectMapper objectMapper;
    private final ServiceCapacityService serviceCapacityService;
    private final Config config;
    
    @GetMapping("/")
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

        serverInfo.setExchangeRateHref("/exchangeRate");

        Capacity capacity = new Capacity();
        capacity.setUnit(config.getCapacityUnit());
        capacity.setValue(serviceCapacityService.getAvailableAmount());
        serverInfo.setCapacities(Arrays.asList(capacity));

        return serverInfo;
    }
}
