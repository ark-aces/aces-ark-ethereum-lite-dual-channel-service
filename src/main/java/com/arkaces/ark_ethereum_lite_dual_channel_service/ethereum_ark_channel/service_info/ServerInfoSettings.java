package com.arkaces.ark_ethereum_lite_dual_channel_service.ethereum_ark_channel.service_info;

import com.arkaces.aces_server.aces_service.server_info.Capacity;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "server-info")
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
