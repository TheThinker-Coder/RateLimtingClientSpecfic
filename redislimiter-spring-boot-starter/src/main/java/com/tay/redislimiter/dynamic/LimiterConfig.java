
package com.tay.redislimiter.dynamic;

import lombok.Data;

@Data
public final class LimiterConfig {
    private String applicationName;
    private String controllerName;
    private String methodName;
    private String baseExp;
    private String path;
    private String timeUnit;
    private int permits;
    private boolean deleted;
}

