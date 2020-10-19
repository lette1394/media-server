package io.lette1394.mediaserver.domain.storage.object;

import static io.lette1394.mediaserver.common.PositiveLong.positiveLong;

import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ObjectFactory {
  private final Storage storage;
  private final ObjectUploadPolicy uploadPolicy = ObjectUploadPolicy.ALL;
  private final ObjectDownloadPolicy downloadPolicy = ObjectDownloadPolicy.ALL;

  public Object create(String area, String key) {
    final Identifier identifier = new Identifier(area, key);

    final Attributes attributes = Attributes.builder()
      .tags(Tags.tags(Tag.tag("k1", "v1"), Tag.tag("k2", "v2")))
      .size(positiveLong(123))
      .created(OffsetDateTime.now())
      .updated(OffsetDateTime.now())
      .build();

    return InitialObject.builder()
      .storage(storage)
      .identifier(identifier)
      .objectUploadPolicy(uploadPolicy)
      .objectDownloadPolicy(downloadPolicy)
      .attributes(attributes)
      .build();
  }
}
