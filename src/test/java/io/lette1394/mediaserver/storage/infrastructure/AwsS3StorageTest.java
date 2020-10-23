package io.lette1394.mediaserver.storage.infrastructure;

import io.lette1394.mediaserver.common.Result;
import io.lette1394.mediaserver.storage.domain.BinarySupplier;
import io.lette1394.mediaserver.storage.domain.Object;
import io.lette1394.mediaserver.storage.domain.ObjectFactory;
import io.lette1394.mediaserver.storage.infrastructure.awss3.AwsClient;
import io.lette1394.mediaserver.storage.infrastructure.awss3.AwsS3Storage;
import io.lette1394.mediaserver.storage.TestBinarySupplier;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.regions.Region;

class AwsS3StorageTest {

  @Test
  @Disabled
  void test1() {
    final AwsS3Storage awsS3Storage = new AwsS3Storage(AwsClient.builder()
      .region(Region.AP_NORTHEAST_2)
      .awsBucket("a-media-server")
      .build());

    final BinarySupplier binarySupplier = new TestBinarySupplier("hello world!!!".getBytes(
      StandardCharsets.UTF_8));
    final ObjectFactory factory = new ObjectFactory(awsS3Storage);
    final Object object = factory.create("h", "123");
    final CompletableFuture<Result<Void>> upload = object.upload(binarySupplier);

    upload.join();
  }

}