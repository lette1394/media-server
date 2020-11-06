package io.lette1394.mediaserver.storage.infrastructure.awss3;

import io.lette1394.mediaserver.common.Contracts;
import io.lette1394.mediaserver.storage.domain.BinarySupplier;
import io.lette1394.mediaserver.storage.infrastructure.ByteBufferPayload;
import io.lette1394.mediaserver.storage.infrastructure.ObjectPath;
import java.util.concurrent.CompletableFuture;
import lombok.Builder;
import lombok.Value;
import reactor.core.publisher.Flux;
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

  <T extends ByteBufferPayload> CompletableFuture<Void> put(ObjectPath objectPath, BinarySupplier<T> binarySupplier) {
    Contracts.require(binarySupplier.length().isPresent(), "aws client needs binary length");

    final S3AsyncClient client = S3AsyncClient.builder()
      .region(region)
      .build();

    return client.putObject(PutObjectRequest.builder()
      .bucket(awsBucket)
      .key(objectPath.asString())
      .contentLength(binarySupplier.length().get()) // content-length required
      .build(), AsyncRequestBody
      // TODO: my publisher interface; then add map() method
      .fromPublisher(Flux.from(binarySupplier.publisher()).map(item -> item.getValue())))
      .thenApply(response -> null);
  }
}
