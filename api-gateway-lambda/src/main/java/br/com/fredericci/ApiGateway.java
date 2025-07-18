package br.com.fredericci;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.function.Function;

@SpringBootApplication
public class ApiGateway {
    
    public static void main(String[] args) {
        SpringApplication.run(ApiGateway.class, args);
    }
    
    @Bean
    public Function<InputEvent, OutputEvent> uppercase() {
        return inputEvent -> {
            System.out.println("Received event: " + inputEvent);
            return new OutputEvent("OK");
        };
    }
    
    public record InputEvent(String type, String id) {
    }
    
    public record OutputEvent(String status) {
    }
}

// curl -H "Content-Type: application/json"  https://pegp5o0vlh.execute-api.eu-west-1.amazonaws.com/dev/execute  -d '{ "type": "FRAUD_DETECTION", "id": "01"}'