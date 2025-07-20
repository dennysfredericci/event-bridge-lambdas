package br.com.fredericci;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Map;
import java.util.function.Function;

@SpringBootApplication
public class EventProcessor {
    
    public static void main(String[] args) {
        SpringApplication.run(EventProcessor.class, args);
    }
    
    @Bean
    public Function<Map<String, Object>, String> processEvent() {
        return event -> {
            System.out.println("Received event: " + event);
            // Add your event processing logic here
            return "Processed";
        };
    }
}

// curl -H "Content-Type: application/json" https://cti08k6ame.execute-api.eu-west-1.amazonaws.com/dev/execute  -d '{ "type": "FRAUD_DETECTION", "id": "01"}'