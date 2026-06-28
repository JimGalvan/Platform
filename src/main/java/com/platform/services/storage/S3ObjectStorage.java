package com.platform.services.storage;

import com.platform.common.storage.ObjectStorage;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class S3ObjectStorage implements ObjectStorage {

    private final Optional<String> bucket;
    private final Region region;
    private final Duration presignedUrlDuration;
    private final Map<String, byte[]> localObjects = new ConcurrentHashMap<>();

    public S3ObjectStorage(
        @ConfigProperty(name = "media.s3.bucket") Optional<String> bucket,
        @ConfigProperty(name = "media.s3.region") String region,
        @ConfigProperty(name = "media.presigned-url-duration") Duration presignedUrlDuration
    ) {
        this.bucket = bucket.filter(value -> !value.isBlank());
        this.region = Region.of(region);
        this.presignedUrlDuration = presignedUrlDuration;
    }

    @Override
    public void put(String objectKey, byte[] bytes, String contentType) {
        if (bucket.isEmpty()) {
            localObjects.put(objectKey, bytes.clone());
            return;
        }
        try (S3Client s3Client = S3Client.builder().region(region).build()) {
            s3Client.putObject(
                PutObjectRequest.builder()
                    .bucket(bucket.orElseThrow())
                    .key(objectKey)
                    .contentType(contentType)
                    .build(),
                RequestBody.fromBytes(bytes)
            );
        }
    }

    @Override
    public String presignedReadUrl(String objectKey) {
        if (objectKey == null) {
            return null;
        }
        if (bucket.isEmpty()) {
            return "http://localhost:8080/media/"
                + URLEncoder.encode(objectKey, StandardCharsets.UTF_8);
        }
        try (S3Presigner presigner = S3Presigner.builder().region(region).build()) {
            GetObjectRequest objectRequest = GetObjectRequest.builder()
                .bucket(bucket.orElseThrow())
                .key(objectKey)
                .build();
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(presignedUrlDuration)
                .getObjectRequest(objectRequest)
                .build();
            return presigner.presignGetObject(presignRequest).url().toString();
        }
    }
}
