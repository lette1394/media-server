package io.lette1394.mediaserver.domain.storage.object;

import java.util.Collections;
import java.util.List;
import lombok.Value;

@Value(staticConstructor = "tags")
public class Tags {
  List<Tag> tags;

  public List<Tag> getTags() {
    return Collections.unmodifiableList(tags);
  }

  public static Tags tags(Tag... tags) {
    return new Tags(List.of(tags));
  }
}
