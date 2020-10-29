package io.lette1394.mediaserver.storage.domain;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Value;

@Value(staticConstructor = "tags")
public class Tags {
  List<Tag> tags;

  public static Tags tags(Tag... tags) {
    return new Tags(Lists.newArrayList(tags));
  }

  public static Tags empty() {
    return new Tags(Lists.newArrayList());
  }

  public List<Tag> getTags() {
    return Collections.unmodifiableList(tags);
  }

  public Map<String, String> toMap() {
    return tags
      .stream()
      .collect(Collectors.toMap(
        tag -> tag.getKey().getValue(),
        Tag::getValue));
  }
}
