package io.lette1394.mediaserver.domain.storage.infrastructure.jpa;

import static io.lette1394.mediaserver.common.NonBlankString.nonBlankString;
import static io.lette1394.mediaserver.common.PositiveLong.positiveLong;

import io.lette1394.mediaserver.domain.storage.object.Attributes;
import io.lette1394.mediaserver.domain.storage.object.FulfilledObject;
import io.lette1394.mediaserver.domain.storage.object.Identifier;
import io.lette1394.mediaserver.domain.storage.object.Object;
import io.lette1394.mediaserver.domain.storage.object.ObjectDownloadPolicy;
import io.lette1394.mediaserver.domain.storage.object.ObjectUploadPolicy;
import io.lette1394.mediaserver.domain.storage.object.PendingObject;
import io.lette1394.mediaserver.domain.storage.object.Storage;
import io.lette1394.mediaserver.domain.storage.object.Tag;
import io.lette1394.mediaserver.domain.storage.object.Tags;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Entity
@AllArgsConstructor
@NoArgsConstructor
class DatabaseStorageObjectEntity {
  @EmbeddedId
  ObjectId objectId;
  State state;
  long sizeInByte;

  // k1=v1,k2=v2,k3=v3,...
  String tagList;

  OffsetDateTime created;
  OffsetDateTime updated;

  Object toObject(Storage storage) {
    if (state == State.PENDING) {
      return createPendingObject(storage);
    }

    if (state == State.FULFILLED) {
      return createFulfilledObject(storage);
    }

    throw new IllegalStateException();
  }

  private FulfilledObject createFulfilledObject(Storage storage) {
    return FulfilledObject.builder()
      .storage(storage)
      .identifier(new Identifier(objectId.area, objectId.key))
      .objectUploadPolicy(ObjectUploadPolicy.ALL)
      .objectDownloadPolicy(ObjectDownloadPolicy.ALL)
      .attributes(Attributes.builder()
        .size(positiveLong(sizeInByte))
        .created(created)
        .updated(updated)
        .tags(parseTags())
        .build())
      .build();
  }

  private PendingObject createPendingObject(Storage storage) {
    return PendingObject.builder()
      .storage(storage)
      .identifier(new Identifier(objectId.area, objectId.key))
      .objectUploadPolicy(ObjectUploadPolicy.ALL)
      .objectDownloadPolicy(ObjectDownloadPolicy.ALL)
      .attributes(Attributes.builder()
        .size(positiveLong(sizeInByte))
        .created(created)
        .updated(updated)
        .tags(parseTags())
        .build())
      .build();
  }

  private Tags parseTags() {
    if (StringUtils.isBlank(tagList)) {
      return Tags.tags(Collections.emptyList());
    }
    return Tags.tags(Arrays
      .stream(tagList.split(","))
      .map(str -> {
        final String[] split = str.split("=");
        return new Tag(nonBlankString(split[0]), nonBlankString(split[1]));
      })
      .collect(Collectors.toList()));
  }

  enum State {
    PENDING, FULFILLED
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
      this(identifier.getArea().getValue(), identifier.getKey().getValue());
    }
  }
}
