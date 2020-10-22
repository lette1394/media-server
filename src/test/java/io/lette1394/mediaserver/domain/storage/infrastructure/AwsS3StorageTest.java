package io.lette1394.mediaserver.domain.storage.infrastructure;

import io.lette1394.mediaserver.domain.storage.TestBinarySupplier;
import io.lette1394.mediaserver.domain.storage.TestFileSystemObjectFactory;
import io.lette1394.mediaserver.domain.storage.infrastructure.filesystem.FileSystemBinaryRepository;
import io.lette1394.mediaserver.domain.storage.object.AutoClosableBinaryRepository;
import io.lette1394.mediaserver.domain.storage.object.BinarySupplier;
import io.lette1394.mediaserver.domain.storage.object.DeleteAllBinaryWhenClosedBinaryRepository;
import io.lette1394.mediaserver.domain.storage.object.Object;
import io.lette1394.mediaserver.domain.storage.object.ObjectFactory;
import io.lette1394.mediaserver.utils.TestObjectFactory;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class AwsS3StorageTest {

  @Test
  @Disabled
  void test1() {
    final AwsS3Storage awsS3Storage = new AwsS3Storage();

    final BinarySupplier binarySupplier = new TestBinarySupplier("hello world!!!".getBytes(
      StandardCharsets.UTF_8));
    final ObjectFactory factory = new ObjectFactory(awsS3Storage);
    final Object object = factory.create("h", "123");
    final CompletableFuture<Void> upload = object.upload(binarySupplier);

    upload.join();
  }

}