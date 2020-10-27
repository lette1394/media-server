package io.lette1394.mediaserver.storage.domain.object;

import io.lette1394.mediaserver.common.TimeStamp;
import io.lette1394.mediaserver.storage.domain.binary.BinaryRepository;
import java.time.OffsetDateTime;

public class Factory {

  private final BinaryRepository binaryRepository;
  private final Policy policy;

  public Factory(BinaryRepository binaryRepository,
    Policy policy) {
    this.binaryRepository = binaryRepository;
    this.policy = policy;
  }

  public Factory(BinaryRepository binaryRepository) {
    this(binaryRepository, Policy.ALL_POLICY);
  }

  public Object create(String area, String key) {
    final Identifier identifier = new Identifier(area, key);

    return InitialObject.builder()
      .identifier(identifier)
      .policy(policy)
      .binaryRepository(binaryRepository)
      .tags(Tags.empty())
      .timeStamp(TimeStamp.builder()
        .created(OffsetDateTime.now())
        .updated(OffsetDateTime.now())
        .build())
      .build();
  }
}
