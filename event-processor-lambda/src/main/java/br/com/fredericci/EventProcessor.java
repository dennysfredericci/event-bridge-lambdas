package br.com.fredericci;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;

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
            
            // Received event: {version=0, id=29844f59-e044-2354-88d4-bebaaa25ec4b, detail-type=InputEvent, source=custom.api.gateway, account=102242991276, time=2025-07-20T16:11:47Z, region=eu-west-1, resources=[], detail={type=FRAUD_DETECTION, id=03}}
            // System.out.println("Received event: " + event);
            
            try (LambdaClient lambdaClient = LambdaClient.create()) {
                
                String downloadFileLambdaArn = System.getenv("DOWNLOAD_FILE_LAMBDA_ARN");
                InvokeRequest request = InvokeRequest.builder()
                        .functionName(downloadFileLambdaArn)
                        .payload(SdkBytes.fromUtf8String(event.toString()))
                        .build();
                
                lambdaClient.invoke(request);
                
            }
            
            return "Processed";
        };
    }
}

// curl -H "Content-Type: application/json" https://cti08k6ame.execute-api.eu-west-1.amazonaws.com/dev/execute  -d '{ "type": "FRAUD_DETECTION", "id": "01"}'