package br.com.fredericci;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Map;
import java.util.function.Function;

@SpringBootApplication
public class DownloadFile {
    
    public static void main(String[] args) {
        SpringApplication.run(DownloadFile.class, args);
    }
    
    @Bean
    public Function<Map<String, Object>, String> processEvent() {
        return event -> {
            // Received event: {version=0, id=29844f59-e044-2354-88d4-bebaaa25ec4b, detail-type=InputEvent, source=custom.api.gateway, account=102242991276, time=2025-07-20T16:11:47Z, region=eu-west-1, resources=[], detail={type=FRAUD_DETECTION, id=03}}
            System.out.println("Received event: " + event);
            // Add your event processing logic here
            return "Processed";
        };
    }
}