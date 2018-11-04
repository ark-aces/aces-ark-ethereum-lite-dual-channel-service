package com.arkaces.ark_ethereum_lite_dual_channel_service.ark_ethereum_channel.config;

import com.arkaces.aces_server.aces_service.server_info.Capacity;
import com.arkaces.ark_ethereum_lite_dual_channel_service.common.server_info.PropertyUrlTemplate;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component("arkEthereumChannel.serverInfoSettings")
@ConfigurationProperties(prefix = "ark-ethereum-channel.server-info")
public class ServerInfoSettings {
    private String name;
    private String description;
    private String version;
    private String websiteUrl;
    private String instructions;
    private List<Capacity> capacities;
    private String inputSchema;
    private String outputSchema;
    private List<PropertyUrlTemplate> outputSchemaUrlTemplates;
    private List<String> interfaces;
}
