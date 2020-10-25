package io.lette1394.mediaserver.storage.infrastructure.jpa;

import static io.lette1394.mediaserver.common.NonBlankString.nonBlankString;
import static io.lette1394.mediaserver.common.PositiveLong.positiveLong;

import io.lette1394.mediaserver.common.TimeStamp;
import io.lette1394.mediaserver.common.UnknownException;
import io.lette1394.mediaserver.storage.domain.binary.BinaryRepository;
import io.lette1394.mediaserver.storage.domain.object.FulfilledObject;
import io.lette1394.mediaserver.storage.domain.object.Identifier;
import io.lette1394.mediaserver.storage.domain.object.Object;
import io.lette1394.mediaserver.storage.domain.object.PendingObject;
import io.lette1394.mediaserver.storage.domain.object.Policy;
import io.lette1394.mediaserver.storage.domain.object.Snapshot;
import io.lette1394.mediaserver.storage.domain.object.State;
import io.lette1394.mediaserver.storage.domain.object.Tag;
import io.lette1394.mediaserver.storage.domain.object.Tags;
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
  State state;
  long sizeInByte;
  long progressingSizeInByte;

  // k1=v1,k2=v2,k3=v3,...
  String tagList;

  OffsetDateTime created;
  OffsetDateTime updated;

  static DatabaseStorageObjectEntity fromObject(Object object) {
    final Snapshot snapshot = object.getSnapshot();

    return DatabaseStorageObjectEntity.builder()
      .state(mapState(object))
      .tagList(fromTags(object.getTags()))
      .sizeInByte(snapshot.getSize()) // TODO: 이거 필드를 두 개 들고 있어야 할 거 같은데...
      .progressingSizeInByte(snapshot.getProgressingSize())
      .objectId(new ObjectId(object.getIdentifier()))
      .created(object.getCreated())
      .updated(OffsetDateTime.now())
      .build();
  }

  private static State mapState(Object object) {
    final Snapshot snapshot = object.getSnapshot();

    if (snapshot.isCompletedNormally()) {
      return State.FULFILLED;
    }
    if (snapshot.isCompletedExceptionally()) {
      if (snapshot.getProgressingSize() > 0) {
        return State.PENDING;
      }
      if (snapshot.getProgressingSize() == 0) {
        return State.INITIAL;
      }
    }
    throw new UnknownException("illegal state");
  }

  private static String fromTags(Tags tags) {
    return tags.toMap()
      .entrySet()
      .stream()
      .map(entry -> String.format("%s=%s", entry.getKey(), entry.getValue()))
      .collect(Collectors.joining(TAG_DELIMITER));
  }

  Object toObject(BinaryRepository binaryRepository) {
    if (state == State.PENDING) {
      return createPendingObject(binaryRepository);
    }

    if (state == State.FULFILLED) {
      return createFulfilledObject(binaryRepository);
    }

    throw new IllegalStateException();
  }

  private FulfilledObject createFulfilledObject(BinaryRepository binaryRepository) {
    return FulfilledObject.builder()
      .binaryRepository(binaryRepository)
      .identifier(new Identifier(objectId.area, objectId.key))
      .policy(Policy.ALL_POLICY)
      .size(positiveLong(sizeInByte))
      .timeStamp(TimeStamp.builder()
        .created(created)
        .updated(updated)
        .build())
      .tags(toTags())
      .build();
  }

  private PendingObject createPendingObject(BinaryRepository binaryRepository) {
    return PendingObject.builder()
      .binaryRepository(binaryRepository)
      .identifier(new Identifier(objectId.area, objectId.key))
      .policy(Policy.ALL_POLICY)
      .size(positiveLong(sizeInByte))
      .timeStamp(TimeStamp.builder()
        .created(created)
        .updated(updated)
        .build())
      .tags(toTags())
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

    public static ObjectId fromIdentifier(Identifier identifier) {
      return new ObjectId(identifier);
    }
  }
}
