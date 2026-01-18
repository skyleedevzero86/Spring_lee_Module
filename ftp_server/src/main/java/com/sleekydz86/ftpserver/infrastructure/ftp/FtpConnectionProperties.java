package com.sleekydz86.ftpserver.infrastructure.ftp;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ftp.server")
public class FtpConnectionProperties {
    private String host = "localhost";
    private int port = 21;
    private String username;
    private String password;
    private PassiveMode passiveMode = new PassiveMode();
    private Timeout timeout = new Timeout();
    private boolean debug = false;
    private Pool pool = new Pool();

    @Data
    public static class PassiveMode {
        private boolean enabled = true;
        private PortRange portRange = new PortRange();
    }

    @Data
    public static class PortRange {
        private int min = 21000;
        private int max = 21010;
    }

    @Data
    public static class Timeout {
        private int connect = 5000;
        private int data = 30000;
    }

    @Data
    public static class Pool {
        private int maxTotal = 10;
        private int maxIdle = 5;
        private int minIdle = 2;
    }

    public boolean isPassiveModeEnabled() {
        return passiveMode != null && passiveMode.isEnabled();
    }

    public int getConnectTimeout() {
        return timeout != null ? timeout.getConnect() : 5000;
    }
}
