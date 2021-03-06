package io.lette1394.mediaserver.storage.infrastructure.jpa;

import static io.lette1394.mediaserver.common.NonBlankString.nonBlankString;

import io.lette1394.mediaserver.storage.domain.BinaryRepository;
import io.lette1394.mediaserver.storage.domain.Identifier;
import io.lette1394.mediaserver.storage.domain.Object;
import io.lette1394.mediaserver.storage.domain.ObjectType;
import io.lette1394.mediaserver.storage.domain.Tag;
import io.lette1394.mediaserver.storage.domain.Tags;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
class DatabaseStorageObjectEntity {
  private static final String TAG_DELIMITER = ",";

  @EmbeddedId
  ObjectId objectId;
  ObjectType objectType;
  long sizeInByte;
  long progressingSizeInByte;

  // k1=v1,k2=v2,k3=v3,...
  String tagList;

  OffsetDateTime created;
  OffsetDateTime updated;

  static DatabaseStorageObjectEntity fromObject(Object object) {
//    final Snapshot snapshot = object.getSnapshot();
//
//    return DatabaseStorageObjectEntity.builder()
//      .type(object.getSnapshot().computeState())
//      .tagList(fromTags(object.getTags()))
//      .sizeInByte(snapshot.getSize()) // TODO: 이거 필드를 두 개 들고 있어야 할 거 같은데...
//      .progressingSizeInByte(snapshot.getProgressingSize())
//      .objectId(new ObjectId(object.getIdentifier()))
//      .created(object.getCreated())
//      .updated(OffsetDateTime.now())
//      .build();
    return null;
  }

  private static String fromTags(Tags tags) {
    return tags.toMap()
      .entrySet()
      .stream()
      .map(entry -> String.format("%s=%s", entry.getKey(), entry.getValue()))
      .collect(Collectors.joining(TAG_DELIMITER));
  }

  Object toObject(BinaryRepository binaryRepository) {
    if (objectType == ObjectType.PENDING) {

    }
    throw new IllegalStateException();
  }

  private Tags toTags() {
    if (StringUtils.isBlank(tagList)) {
      return Tags.tags(Collections.emptySet());
    }
    return Tags.tags(Arrays
      .stream(tagList.split(TAG_DELIMITER))
      .map(str -> {
        final String[] split = str.split("=");
        return new Tag(split[0], split[1]);
      })
      .collect(Collectors.toSet()));
  }

  @Data
  @Embeddable
  @AllArgsConstructor
  @NoArgsConstructor
  public static class ObjectId implements Serializable {
    private static final long serialVersionUID = -179586409209928693L;
    String area;
    String key;

    public ObjectId(Identifier identifier) {
      this(identifier.getArea(), identifier.getKey());
    }

    public static ObjectId fromIdentifier(Identifier identifier) {
      return new ObjectId(identifier);
    }
  }
}
