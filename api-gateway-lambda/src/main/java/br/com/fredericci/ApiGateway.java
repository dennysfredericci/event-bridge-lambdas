package br.com.fredericci;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequest;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequestEntry;

import java.util.function.Function;

@SpringBootApplication
public class ApiGateway {
    
    private ObjectMapper objectMapper = new ObjectMapper();
    
    public static void main(String[] args) {
        SpringApplication.run(ApiGateway.class, args);
    }
    
    @Bean
    public Function<InputEvent, OutputEvent> publishEvent() {
        
        return inputEvent -> {
            try (EventBridgeClient eventBridgeClient = EventBridgeClient.create()) {
                PutEventsRequestEntry entry = PutEventsRequestEntry.builder()
                        .eventBusName(System.getenv("EVENT_BUS_NAME"))
                        .source("custom.api.gateway")
                        .detailType("InputEvent")
                        .detail(objectMapper.writeValueAsString(inputEvent))
                        .build();
                
                PutEventsRequest request = PutEventsRequest.builder()
                        .entries(entry)
                        .build();
                
                eventBridgeClient.putEvents(request);
                System.out.println("Event published to EventBridge: " + inputEvent);
            } catch (Exception e) {
                e.printStackTrace();
                return new OutputEvent("ERROR");
            }
            return new OutputEvent("OK");
        };
    }
    
    public record InputEvent(String id) {
    }
    
    public record OutputEvent(String status) {
    }
}

// curl -H "Content-Type: application/json" https://cti08k6ame.execute-api.eu-west-1.amazonaws.com/dev/execute  -d '{ "type": "FRAUD_DETECTION", "id": "01"}'