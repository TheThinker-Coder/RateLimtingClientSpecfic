package com.tay.demo1;

import com.tay.redislimiter.event.RateCheckFailureEvent;
import com.tay.redislimiter.event.RateCheckFailureListener;
import org.springframework.stereotype.Component;

@Component
public class MyCheckFailureListener implements RateCheckFailureListener {
    public void onApplicationEvent(RateCheckFailureEvent event) {
        System.out.println("my check ####" + event.getMsg());
    }
}
