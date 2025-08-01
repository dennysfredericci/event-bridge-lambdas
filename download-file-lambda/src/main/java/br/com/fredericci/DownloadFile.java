package br.com.fredericci;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@SpringBootApplication
public class DownloadFile {
    
    private ObjectMapper objectMapper = null;
    
    public DownloadFile() {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        builder.modulesToInstall(new JavaTimeModule());
        objectMapper = builder.build();
    }
    
    public static void main(String[] args) {
        SpringApplication.run(DownloadFile.class, args);
    }
    
    @Bean
    public Function<EventBridgeEvent<InputEvent>, String> processEvent() {
        return event -> {
            // Received event: {version=0, id=29844f59-e044-2354-88d4-bebaaa25ec4b, detail-type=InputEvent, source=custom.api.gateway, account=102242991276, time=2025-07-20T16:11:47Z, region=eu-west-1, resources=[], detail={type=FRAUD_DETECTION, id=03}}
            System.out.println("Received event: " + event);
            System.out.println("Detail: " + event.detail());
            
            InputEvent inputEvent = event.detail();
            System.out.println(inputEvent.id());
            
            String bucketName = System.getenv("DOCUMENT_BUCKET_NAME");
            String key = LocalDate.now() + "/" + inputEvent.id();
            String fileUrl = "https://freetestdata.com/wp-content/uploads/2025/03/Free_Test_Data_3MB_PDF.pdf";
            
            uploadToS3(bucketName, key, fileUrl);
            
            return "Processed";
        };
    }
    
    public void uploadToS3(String bucketName, String key, String fileUrl) {
        try (S3Client s3Client = S3Client.create()) {
            // Download file from URL
            java.net.URL url = new java.net.URL(fileUrl);
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            
            if (connection.getResponseCode() != 200) {
                throw new RuntimeException("Failed to download file: HTTP " + connection.getResponseCode());
            }
            
            byte[] fileContent = connection.getInputStream().readAllBytes();
            
            // Upload file to S3
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(fileContent));
            System.out.println("File downloaded from URL and uploaded to S3: " + key);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to download and upload file to S3", e);
        }
    }
    
    public record EventBridgeEvent<T>(
            int version,
            UUID id,
            String detailType,
            String source,
            String account,
            Instant time,
            String region,
            List<String> resources,
            T detail
    ) {
    }
    
    public record InputEvent(String id) {
    }
}