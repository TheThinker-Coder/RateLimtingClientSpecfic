
package com.tay.demo1;

import com.tay.redislimiter.RateLimiter;
import com.tay.redislimiter.dynamic.DynamicRateLimiter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;


@RestController
@RequestMapping("/demo")
public class DemoController {
    
    @GetMapping("/test")
    
    @RateLimiter(base = "#Headers['userid']", permits = 2, timeUnit = TimeUnit.MINUTES) 
    public String test() {
        return "test!";
    }

//    @GetMapping("/dynamictest")
//
//    @DynamicRateLimiter(base = "#Headers['x-real-ip']", permits = 5, timeUnit = TimeUnit.MINUTES)
//    public String dynamicTest() {
//        return "dynamictest!";
//    }
}
