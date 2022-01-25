
package com.tay.redislimiter.event;

import org.springframework.context.ApplicationListener;

public interface RateCheckFailureListener extends ApplicationListener<RateCheckFailureEvent> {

}
