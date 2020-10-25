package io.lette1394.mediaserver.storage.infrastructure.jpa;

import static io.lette1394.mediaserver.common.NonBlankString.nonBlankString;
import static io.lette1394.mediaserver.common.PositiveLong.positiveLong;

import io.lette1394.mediaserver.storage.domain.Attributes;
import io.lette1394.mediaserver.storage.domain.BinaryRepository;
import io.lette1394.mediaserver.storage.domain.FulfilledObject;
import io.lette1394.mediaserver.storage.domain.Identifier;
import io.lette1394.mediaserver.storage.domain.Object;
import io.lette1394.mediaserver.storage.domain.ObjectPolicy;
import io.lette1394.mediaserver.storage.domain.Snapshot;
import io.lette1394.mediaserver.storage.domain.ObjectState;
import io.lette1394.mediaserver.storage.domain.PendingObject;
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
  ObjectState state;
  long sizeInByte;

  // k1=v1,k2=v2,k3=v3,...
  String tagList;

  OffsetDateTime created;
  OffsetDateTime updated;

  static DatabaseStorageObjectEntity fromObject(Object object) {
    return DatabaseStorageObjectEntity.builder()
      .state(mapState(object))
      .tagList(fromTags(object.getTags()))
      .sizeInByte(object.getSnapshot().getProgressingSize()) // TODO: 이거 필드를 두 개 들고 있어야 할 거 같은데...
      .objectId(new ObjectId(object.identifier))
      .created(object.getCreated())
      .updated(OffsetDateTime.now())
      .build();
  }

  private static ObjectState mapState(Object object) {
    final Snapshot snapshot = object.getSnapshot();
    final long size = snapshot.getSize();
    final long progressSize = snapshot.getProgressingSize();
    if (size == 0 && progressSize == 0) {
      return ObjectState.INITIAL;
    }
    if (size == 0 && progressSize > 0) {
      return ObjectState.PENDING;
    }

    if (size == 0) {
      return ObjectState.INITIAL;
    }


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
    if (state == ObjectState.PENDING) {
      return createPendingObject(binaryRepository);
    }

    if (state == ObjectState.FULFILLED) {
      return createFulfilledObject(binaryRepository);
    }

    throw new IllegalStateException();
  }

  private FulfilledObject createFulfilledObject(BinaryRepository binaryRepository) {
    return FulfilledObject.builder()
      .binaryRepository(binaryRepository)
      .identifier(new Identifier(objectId.area, objectId.key))
      .objectPolicy(ObjectPolicy.ALL_POLICY)
      .size(positiveLong(sizeInByte))
      .attributes(Attributes.builder()
        .created(created)
        .updated(updated)
        .tags(toTags())
        .build())
      .build();
  }

  private PendingObject createPendingObject(BinaryRepository binaryRepository) {
    return PendingObject.builder()
      .binaryRepository(binaryRepository)
      .identifier(new Identifier(objectId.area, objectId.key))
      .objectPolicy(ObjectPolicy.ALL_POLICY)
      .size(positiveLong(sizeInByte))
      .attributes(Attributes.builder()
        .created(created)
        .updated(updated)
        .tags(toTags())
        .build())
      .build();
  }

  private Tags toTags() {
    if (StringUtils.isBlank(tagList)) {
      return Tags.tags(Collections.emptyList());
    }
    return Tags.tags(Arrays
      .stream(tagList.split(TAG_DELIMITER))
      .map(str -> {
        final String[] split = str.split("=");
        return new Tag(nonBlankString(split[0]), split[1]);
      })
      .collect(Collectors.toList()));
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
  }
}
