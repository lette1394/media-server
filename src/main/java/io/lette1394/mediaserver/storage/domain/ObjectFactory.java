package io.lette1394.mediaserver.storage.domain;

import io.lette1394.mediaserver.common.TimeStamp;
import io.lette1394.mediaserver.storage.infrastructure.Publishers;
import java.time.OffsetDateTime;
import java.util.function.Function;
import lombok.Value;
import org.reactivestreams.Publisher;

@Value
public class ObjectFactory<BUFFER extends SizeAware> {

  ObjectPolicy objectPolicy;
  BinaryPolicy binaryPolicy;

  public <T> Object<BUFFER> create(String area, String key, Publisher<T> publisher,
    Function<T, BUFFER> mapper) {
    final Identifier identifier = new Identifier(area, key);

    return InitialObject.<BUFFER>builder()
      .identifier(identifier)
      .objectPolicy(objectPolicy)
      .binaryPolicy(binaryPolicy)
      .binarySupplier(() -> Publishers.convert(publisher, mapper))
      .binarySnapshot(BinarySnapshot.initial())
      .tags(Tags.empty())
      .timeStamp(TimeStamp.builder()
        .created(OffsetDateTime.now())
        .updated(OffsetDateTime.now())
        .build())
      .build();
  }
}
