package org.opengroup.osdu.file.provider.reference.factory;

import io.minio.MinioClient;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.file.provider.reference.config.MinioConfigProperties;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Lazy
@Slf4j
@RequiredArgsConstructor
public class CloudObjectStorageFactory {

  private final MinioConfigProperties minIoConfigProperties;
  private MinioClient minioClient;

  @PostConstruct
  public void init() {
    minioClient = MinioClient.builder()
        .endpoint(minIoConfigProperties.getEndpointUrl())
        .credentials(minIoConfigProperties.getAccessKey(),
            minIoConfigProperties.getSecretKey())
        .region(minIoConfigProperties.getRegion())
        .build();
    log.info("Minio client initialized on {}", minIoConfigProperties.getEndpointUrl());
  }

  public MinioClient getClient() {
    return this.minioClient;
  }

  public void setMinioClient(MinioClient minioClient) {
    this.minioClient = minioClient;
  }
}
