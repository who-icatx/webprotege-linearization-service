package edu.stanford.protege.webprotege.linearizationservice;

import java.util.concurrent.TimeUnit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import edu.stanford.protege.webprotege.ipc.WebProtegeIpcApplication;
import io.minio.MinioClient;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;

@SpringBootApplication
@Import({WebProtegeIpcApplication.class})
public class WebprotegeLinearizationServiceServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebprotegeLinearizationServiceServiceApplication.class, args);
    }


    @Bean
    MinioClient minioClient(MinioProperties properties) {
        return MinioClient.builder()
                          .credentials(properties.getAccessKey(), properties.getSecretKey())
                          .endpoint(properties.getEndPoint())
                          .httpClient(new OkHttpClient.Builder()
                              .connectTimeout(properties.getConnectionTimeout(), TimeUnit.MILLISECONDS)
                              .readTimeout(properties.getReadTimeout(), TimeUnit.MILLISECONDS)
                              .writeTimeout(properties.getWriteTimeout(), TimeUnit.MILLISECONDS)
                              .connectionPool(new ConnectionPool(10, 5, TimeUnit.MINUTES))
                              .retryOnConnectionFailure(true)
                              .build())
                          .build();
    }
}
