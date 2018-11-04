package com.arkaces.ark_ethereum_lite_dual_channel_service;

import ark_java_client.*;
import ark_java_client.lib.ResourceUtils;
import com.arkaces.aces_server.aces_service.config.AcesServiceConfig;
import com.arkaces.aces_server.aces_service.notification.NotificationService;
import com.arkaces.aces_server.aces_service.notification.NotificationServiceFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.env.Environment;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.mail.MailSender;
import org.springframework.web.client.RestTemplate;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;

@Configuration
@Import({AcesServiceConfig.class})
public class ApplicationConfig {

    @Bean
    public ArkClient arkClient(Environment environment) {
        ArkNetworkFactory arkNetworkFactory = new ArkNetworkFactory();
        String arkNetworkConfigPath = environment.getProperty("arkNetworkConfigPath");
        ArkNetwork arkNetwork = arkNetworkFactory.createFromYml(arkNetworkConfigPath);

        HttpArkClientFactory httpArkClientFactory = new HttpArkClientFactory();
        return httpArkClientFactory.create(arkNetwork);
    }

    @Bean
    public Integer arkMinConfirmations(Environment environment) {
        return environment.getProperty("arkMinConfirmations", Integer.class);
    }

    @Bean(name = "applicationEventMulticaster")
    public ApplicationEventMulticaster simpleApplicationEventMulticaster() {
        SimpleApplicationEventMulticaster eventMulticaster = new SimpleApplicationEventMulticaster();
        eventMulticaster.setTaskExecutor(new SimpleAsyncTaskExecutor());
        
        return eventMulticaster;
    }

    @Bean
    @ConditionalOnProperty(value = "notifications.enabled", havingValue = "true")
    public NotificationService emailNotificationService(Environment environment, MailSender mailSender) {
        return new NotificationServiceFactory().createEmailNotificationService(
                environment.getProperty("serverInfo.name"),
                environment.getProperty("notifications.fromEmailAddress"),
                environment.getProperty("notifications.recipientEmailAddress"),
                mailSender
        );
    }

    @Bean
    @ConditionalOnProperty(value = "notifications.enabled", havingValue = "false", matchIfMissing = true)
    public NotificationService noOpNotificationService() {
        return new NotificationServiceFactory().createNoOpNotificationService();
    }

    @Bean
    public BigDecimal lowCapacityThreshold(Environment environment) {
        return environment.getProperty("lowCapacityThreshold", BigDecimal.class);
    }



    // ethereum listener

    @Bean
    public Integer maxScanBlockDepth(Environment environment) {
        return environment.getProperty("maxScanBlockDepth", Integer.class);
    }

    @Bean
    public RestTemplate ethereumRpcRestTemplate(Environment environment) {
        return new RestTemplateBuilder()
                .rootUri(environment.getProperty("ethereumRpc.url"))
                .build();
    }


}
