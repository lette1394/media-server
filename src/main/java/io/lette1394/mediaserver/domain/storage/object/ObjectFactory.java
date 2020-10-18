package io.lette1394.mediaserver.domain.storage.object;

import static java.util.concurrent.CompletableFuture.completedFuture;

import io.lette1394.mediaserver.common.PositiveLong;
import java.time.OffsetDateTime;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ObjectFactory {
  private final Storage storage;
  private final ObjectUploadPolicy uploadPolicy = ObjectUploadPolicy.ALL;
  private final ObjectDownloadPolicy downloadPolicy = ObjectDownloadPolicy.ALL;

  public CompletableFuture<Object> create(String area, String key) {
    final Identifier identifier = new Identifier(area, key);

    final Attributes attributes = Attributes.builder()
      .tags(Tags.tags(Tag.tag("k1", "v1"), Tag.tag("k2", "v2")))
      .size(PositiveLong.positiveLong(123))
      .created(OffsetDateTime.now())
      .updated(OffsetDateTime.now())
      .build();

    return storage.isExist(identifier)
      .thenCompose(exist -> {
        if (exist) {
          return storage.find(identifier);
        }

        return completedFuture(
          InitialObject.builder()
            .storage(storage)
            .identifier(identifier)
            .objectUploadPolicy(uploadPolicy)
            .objectDownloadPolicy(downloadPolicy)
            .attributes(attributes)
            .build());
      });
  }
}
