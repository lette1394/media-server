package io.lette1394.mediaserver.domain.storage.infrastructure.jpa;

import io.lette1394.mediaserver.domain.storage.object.Object;
import io.lette1394.mediaserver.domain.storage.object.PendingObject;
import java.time.OffsetDateTime;
import javax.persistence.Entity;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
class DatabaseStorageObjectEntity {
  @Id
  String object_id;
  State state;

  String tag_list;
  long size_in_byte;

  OffsetDateTime created;
  OffsetDateTime updated;

  enum State {
    PENDING, FULFILLED
  }

  Object toObject() {
    // TODO: use object factory
    if (state == State.PENDING) {
      return PendingObject.builder()
        .build();
    }
    return null;
  }
}
