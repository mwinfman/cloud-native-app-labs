package io.pivotal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class GreetingRibbonRestTemplateApplication {
	
    public static void main(String[] args) {
        SpringApplication.run(GreetingRibbonRestTemplateApplication.class, args);
    }
 
}
