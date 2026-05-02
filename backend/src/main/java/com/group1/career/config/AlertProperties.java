package com.group1.career.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * F27: Alert configuration, bound from {@code alert.*} in application yml.
 */
@Data
@Component
@ConfigurationProperties(prefix = "alert")
public class AlertProperties {

    private ServerChan serverChan = new ServerChan();
    private String email = "";

    @Data
    public static class ServerChan {
        private boolean enabled = false;
        private String sendKey = "";
    }
}
