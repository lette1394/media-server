package io.lette1394.mediaserver.storage.domain;

import static io.lette1394.mediaserver.common.NonBlankString.nonBlankString;

import com.google.common.collect.Sets;
import io.lette1394.mediaserver.common.NonBlankString;
import io.lette1394.mediaserver.common.TimeStamp;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

public class ObjectFactory<BUFFER extends Payload> {

  ObjectPolicy objectPolicy;
  BinaryPolicy binaryPolicy;

  public ObjectFactory(ObjectPolicy objectPolicy,
    BinaryPolicy binaryPolicy) {
    this.objectPolicy = objectPolicy;
    this.binaryPolicy = binaryPolicy;
  }

  public ObjectFactory() {
    this(ObjectPolicy.ALL_OBJECT_POLICY, BinaryPolicy.ALL_BINARY_POLICY);
  }

  public <T> Object<BUFFER> create(Identifier identifier, String tagKey0, String tagValue0) {
    final Tags tags = Tags.tags(Sets.newHashSet(new Tag(tagKey0, tagValue0)));
    return create(identifier.getArea(), identifier.getKey(), tags);
  }

  public <T> Object<BUFFER> create(Identifier identifier) {
    return create(identifier.getArea(), identifier.getKey(), Tags.empty());
  }

  public <T> Object<BUFFER> create(String area, String key, Tags tags) {
    final Identifier identifier = new Identifier(area, key);

    return Object.<BUFFER>builder()
      .identifier(identifier)
      .objectPolicy(objectPolicy)
      .objectSnapshot(ObjectSnapshot.initial())
      .binaryPolicy(binaryPolicy)
      .binarySnapshot(BinarySnapshot.initial())
      .tags(tags)
      .timeStamp(TimeStamp.builder()
        .created(OffsetDateTime.now())
        .updated(OffsetDateTime.now())
        .build())
      .build();
  }

//  public static class ObjectBuilder {
//    private final Set<Tag> tags = new HashSet<>();
//
//    public ObjectBuilder tag(String key, String value) {
//      tags.add(new Tag(key, value));
//      return this;
//    }
//
//    public ObjectBuilder tag(String key) {
//      tags.add(new Tag(key));
//      return this;
//    }
//
//    public <BUFFER extends Payload> Object<BUFFER> build() {
//      return
//    }
//  }
}
