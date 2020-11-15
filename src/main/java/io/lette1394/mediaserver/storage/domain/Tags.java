package io.lette1394.mediaserver.storage.domain;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.lette1394.mediaserver.storage.domain.Tag.EmptyTag;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;


// TODO: tag 가 생성 / 저장될 때
//  prefix 로 [system, user] 등으로 나눠서
//  코드 내부에서 사용되는 tag와, user가 등록한 tag를 구분하도록 하자
@Value(staticConstructor = "tags")
public class Tags {

  Map<String, Tag> tags;

  public static Tags tags(Tag... tags) {
    return tags(Sets.newHashSet(tags));
  }

  public static Tags tags(Set<Tag> tags) {
    final Map<String, Tag> collect = tags.stream()
      .collect(Collectors.toMap(
        tag -> tag.getKey(),
        tag -> tag
      ));
    return new Tags(collect);
  }

  public static Tags empty() {
    return new Tags(Maps.newHashMap());
  }

  public Set<Tag> getTags() {
    return Collections.unmodifiableSet(Sets.newHashSet(tags.values()));
  }

  public Map<String, String> toMap() {
    return tags.entrySet()
      .stream()
      .collect(Collectors.toMap(
        entry -> entry.getKey(),
        entry -> defaultIfBlank(entry.getValue().getValue(), "")));
  }

  public boolean has(String key) {
    return tags.containsKey(key);
  }

  public Tag get(String key) {
    if (tags.containsKey(key)) {
      return tags.get(key);
    }
    return EmptyTag.INSTANCE;
  }

  public void addTag(String key, String value) {
    tags.put(key, new Tag(key, value));
  }

  public void addTag(String key) {
    tags.put(key, new Tag(key));
  }

  public void addAllTag(Tags others) {
    this.tags.putAll(others.tags);
  }
}
