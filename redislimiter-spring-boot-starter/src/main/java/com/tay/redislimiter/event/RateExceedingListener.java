package com.tay.redislimiter.event;

import org.springframework.context.ApplicationListener;

public interface RateExceedingListener extends ApplicationListener<RateExceedingEvent> {
}
