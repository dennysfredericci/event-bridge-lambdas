package br.com.fredericci;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@SpringBootApplication
public class EventProcessor {
    
    private ObjectMapper objectMapper = null;
    
    public EventProcessor() {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        builder.modulesToInstall(new JavaTimeModule());
        objectMapper = builder.build();
    }
    
    public static void main(String[] args) {
        SpringApplication.run(EventProcessor.class, args);
    }
    
    @Bean
    public Function<EventBridgeEvent<?>, String> processEvent() {
        return event -> {
            
            // Received event: {version=0, id=29844f59-e044-2354-88d4-bebaaa25ec4b, detail-type=InputEvent, source=custom.api.gateway, account=102242991276, time=2025-07-20T16:11:47Z, region=eu-west-1, resources=[], detail={type=FRAUD_DETECTION, id=03}}
            System.out.println("Received event: " + event);
            
            
            
            if (!event.source().equals("aws.scheduler")) {
                try (LambdaClient lambdaClient = LambdaClient.create()) {
                    
                    String json = objectMapper.writeValueAsString(event);
                    
                    System.out.println("Json Payload:" + json);
                    
                    String downloadFileLambdaArn = System.getenv("DOWNLOAD_FILE_LAMBDA_ARN");
                    InvokeRequest request = InvokeRequest.builder()
                            .functionName(downloadFileLambdaArn)
                            .payload(SdkBytes.fromUtf8String(json))
                            .build();
                    
                    lambdaClient.invoke(request);
                    
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
            

            
            return "Processed";
        };
    }
    
    /* Create a presigned URL to use in a subsequent PUT request */
    public String createPresignedUrl(String bucketName, String keyName, Map<String, String> metadata) {
        try (S3Presigner presigner = S3Presigner.create()) {
            
            PutObjectRequest objectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(keyName)
                    .metadata(metadata)
                    .build();
            
            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(10))  // The URL expires in 10 minutes.
                    .putObjectRequest(objectRequest)
                    .build();
            
            
            PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(presignRequest);
            String myURL = presignedRequest.url().toString();
            System.out.println("Presigned URL to upload a file to: " + myURL);
            System.out.println("HTTP method: " + presignedRequest.httpRequest().method());
            
            return presignedRequest.url().toExternalForm();
        }
    }
    
    public record EventBridgeEvent<T>(
            int version,
            UUID id,
            String detailType,
            String source,
            String account,
            LocalDateTime time,
            String region,
            List<String> resources,
            T detail
    ) {
    }
}

// curl -H "Content-Type: application/json" XXXX -d '{ "type": "FRAUD_DETECTION", "id": "01"}'



/**
 *
 * {
 *     "version": "0",
 *     "id": "aca16717-3610-96e6-0290-07ba46f4b9e8",
 *     "detail-type": "InputEvent",
 *     "source": "custom.api.gateway",
 *     "account": "102242991276",
 *     "time": "2025-07-25T04:03:10Z",
 *     "region": "eu-west-1",
 *     "resources": [],
 *     "detail": {
 *         "id": "01"
 *     }
 * }
 *
 * --
 *
 * {
 *     "version": "0",
 *     "id": "d3688300-50bf-4480-8c10-c18ac7186c12",
 *     "detail-type": "Scheduled Event",
 *     "source": "aws.scheduler",
 *     "account": "102242991276",
 *     "time": "2025-07-25T03:56:00Z",
 *     "region": "eu-west-1",
 *     "resources": [
 *         "arn:aws:scheduler:eu-west-1:102242991276:schedule/default/MyDailyLambdaSchedule"
 *     ],
 *     "detail": "{}"
 * }
 *
 *
 */