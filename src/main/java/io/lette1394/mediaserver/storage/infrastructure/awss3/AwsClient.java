package io.lette1394.mediaserver.storage.infrastructure.awss3;

import io.lette1394.mediaserver.common.Result;
import io.lette1394.mediaserver.storage.infrastructure.ObjectPath;
import io.lette1394.mediaserver.storage.domain.BinarySupplier;
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

  CompletableFuture<Result<Void>> put(ObjectPath objectPath, BinarySupplier binarySupplier) {
    final S3AsyncClient client = S3AsyncClient.builder()
      .region(region)
      .build();

    return client.putObject(PutObjectRequest.builder()
      .bucket(awsBucket)
      .key(objectPath.asString())
      .contentLength(binarySupplier.length())
      .build(), AsyncRequestBody
      .fromPublisher(s -> binarySupplier.getAsync()))
      .thenApply(aVoid -> Result.succeed());
  }
}
