package com.arkaces.ark_ethereum_lite_dual_channel_service.common.server_info;

import com.arkaces.aces_server.aces_service.server_info.Capacity;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ServerInfo {
    private String name;
    private String description;
    private String version;
    private String websiteUrl;
    private String instructions;
    private List<Capacity> capacities;
    private BigDecimal flatFee;
    private String flatFeeUnit;
    private BigDecimal percentFee;
    private JsonNode inputSchema;
    private JsonNode outputSchema;
    private List<PropertyUrlTemplate> outputSchemaUrlTemplates;
    private String exchangeRateHref;
    private List<String> interfaces;
}
