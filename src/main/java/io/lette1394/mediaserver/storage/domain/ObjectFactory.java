package io.lette1394.mediaserver.storage.domain;

import com.google.common.collect.Sets;
import io.lette1394.mediaserver.common.TimeStamp;
import java.time.OffsetDateTime;
import lombok.Builder;

public class ObjectFactory<P extends Payload> {

  private final ObjectPolicy objectPolicy;
  private final BinaryPolicy binaryPolicy;

  private final BinaryRepository<P> binaryRepository;

  @Builder
  public ObjectFactory(ObjectPolicy objectPolicy,
    BinaryPolicy binaryPolicy,
    BinaryRepository<P> binaryRepository) {
    this.objectPolicy = objectPolicy;
    this.binaryPolicy = binaryPolicy;
    this.binaryRepository = binaryRepository;
  }

  public ObjectFactory(BinaryRepository<P> binaryRepository) {
    this(ObjectPolicy.ALL_OBJECT_POLICY, BinaryPolicy.ALL_BINARY_POLICY, binaryRepository);
  }

  public ObjectFactory<P> with(BinaryRepository<P> binaryRepository) {
    return new ObjectFactory<>(objectPolicy, binaryPolicy, binaryRepository);
  }

  public <T> Object<P> create(Identifier identifier, String tagKey0, String tagValue0) {
    final Tags tags = Tags.tags(Sets.newHashSet(new Tag(tagKey0, tagValue0)));
    return create(identifier, tags);
  }

  public <T> Object<P> create(Identifier identifier) {
    return create(identifier, BinaryPath.from(identifier), Tags.empty());
  }

  public <T> Object<P> create(Identifier identifier, Tags tags) {
    return create(identifier, BinaryPath.from(identifier), tags);
  }

  public <T> Object<P> create(String area, String key) {
    final Identifier id = new Identifier(area, key);
    return create(id, BinaryPath.from(id), Tags.empty());
  }

  public <T> Object<P> create(Identifier identifier, BinaryPath binaryPath, Tags tags) {
    return Object.<P>builder()
      .identifier(identifier)
      .binaryPath(binaryPath)
      .objectPolicy(objectPolicy)
      .objectSnapshot(ObjectSnapshot.initial())
      .binaryPolicy(binaryPolicy)
      .binarySnapshot(BinarySnapshot.initial())
      .tags(tags)
      .binaryRepository(binaryRepository)
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
//    public <P extends Payload> Object<P> build() {
//      return
//    }
//  }
}
