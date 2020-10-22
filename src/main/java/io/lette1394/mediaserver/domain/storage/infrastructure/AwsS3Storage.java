package io.lette1394.mediaserver.domain.storage.infrastructure;

import io.lette1394.mediaserver.common.Result;
import io.lette1394.mediaserver.domain.storage.object.BinarySupplier;
import io.lette1394.mediaserver.domain.storage.object.Identifier;
import io.lette1394.mediaserver.domain.storage.object.Object;
import io.lette1394.mediaserver.domain.storage.object.PendingObject;
import io.lette1394.mediaserver.domain.storage.object.Storage;
import io.lette1394.mediaserver.domain.storage.usecase.ObjectNotFoundException;
import java.util.concurrent.CompletableFuture;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class AwsS3Storage implements Storage {
  @Override
  public CompletableFuture<Boolean> objectExists(Identifier identifier)
    throws ObjectNotFoundException {
    return CompletableFuture.completedFuture(false);
  }

  @Override
  public CompletableFuture<Object> findObject(Identifier identifier)
    throws ObjectNotFoundException {

    return CompletableFuture.completedFuture(PendingObject.builder().build());
  }

  @Override
  public CompletableFuture<BinarySupplier> findBinary(
    Identifier identifier) {
    return CompletableFuture.failedFuture(new RuntimeException());
  }

  @Override
  public CompletableFuture<Result> createBinary(Identifier identifier, BinarySupplier binarySupplier) {
    final Region region = Region.AP_NORTHEAST_2;

    final S3AsyncClient client = S3AsyncClient.builder()
      .region(region)
      .build();

    return client.putObject(PutObjectRequest.builder()
      .bucket("a-media-server")
      .key("wowgogo")
      // TODO: content length를 꼭 받아야 하는구나... 이럴수가
      //  이게 어쩔수 없는건가? stream 이라서 전체 길이를 모르니까? http chunk 방식은 끝에 뭐가 올지 아는데.. 음..
      .contentLength(14L)
      .build(), AsyncRequestBody
      .fromPublisher(s -> binarySupplier.getAsync()))
      .thenAccept(ss -> {
        System.out.println();
      })
      .thenApply(aVoid -> Result.succeed());
  }

  @Override
  public CompletableFuture<Result> appendBinary(Identifier identifier, BinarySupplier binarySupplier) {
    return null;
  }

  @Override
  public CompletableFuture<Result> deleteBinary(Identifier identifier) {
    return null;
  }
}
