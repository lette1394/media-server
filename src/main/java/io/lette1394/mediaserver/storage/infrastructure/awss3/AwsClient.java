package io.lette1394.mediaserver.storage.infrastructure.awss3;

import io.lette1394.mediaserver.storage.domain.binary.LengthAwareBinarySupplier;
import io.lette1394.mediaserver.storage.infrastructure.ObjectPath;
import java.util.concurrent.CompletableFuture;
import lombok.Builder;
import lombok.Value;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Value
public class AwsClient {
  String awsBucket;
  Region region;
  S3AsyncClient client;

  @Builder
  public AwsClient(String awsBucket, Region region) {
    this.awsBucket = awsBucket;
    this.region = region;

    this.client = S3AsyncClient.builder()
      .region(region)
      .build();
  }

  CompletableFuture<Void> put(ObjectPath objectPath, LengthAwareBinarySupplier binaries) {
    final S3AsyncClient client = S3AsyncClient.builder()
      .region(region)
      .build();

    return client.putObject(PutObjectRequest.builder()
      .bucket(awsBucket)
      .key(objectPath.asString())
      .contentLength(binaries.getLength()) // content-length required
      .build(), AsyncRequestBody
      .fromPublisher(binaries.getAsync()))
      .thenApply(response -> null);
  }
}
