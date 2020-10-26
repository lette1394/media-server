package io.lette1394.mediaserver.storage.infrastructure.filesystem;

import static io.lette1394.mediaserver.common.NonBlankString.nonBlankString;
import static io.lette1394.mediaserver.common.PositiveLong.positiveLong;
import static java.lang.Long.parseLong;
import static java.lang.String.format;

import io.lette1394.mediaserver.common.TimeStamp;
import io.lette1394.mediaserver.storage.domain.binary.BinaryRepository;
import io.lette1394.mediaserver.storage.domain.binary.BinarySupplier;
import io.lette1394.mediaserver.storage.domain.object.FulfilledObject;
import io.lette1394.mediaserver.storage.domain.object.Identifier;
import io.lette1394.mediaserver.storage.domain.object.Object;
import io.lette1394.mediaserver.storage.domain.object.Policy;
import io.lette1394.mediaserver.storage.domain.object.Snapshot;
import io.lette1394.mediaserver.storage.domain.object.State;
import io.lette1394.mediaserver.storage.domain.object.Tag;
import io.lette1394.mediaserver.storage.domain.object.Tags;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;

@Value
public class FileSystemObjectEntity {
  private static final String LINE_SEPARATOR = "\n";
  Object object;

  public static FileSystemObjectEntity fromBytes(byte[] bytes, BinaryRepository<? super BinarySupplier> binaryRepository) {
    try {
      final String raw = new String(bytes);
      final Map<String, String> map = Arrays.stream(raw.split("\n"))
        .map(line -> line.split(":"))
        .reduce(new HashMap<>(), (acc, cur) -> {
          final String key = cur[0];
          final String value = cur[1];
          acc.put(key, value);
          return acc;
        }, (stringStringHashMap, stringStringHashMap2) -> {
          throw new RuntimeException();
        });

      final List<Tag> tags = map.entrySet().stream()
        .filter(entry -> entry.getKey().startsWith("tag"))
        .map(entry -> new Tag(nonBlankString(entry.getKey().substring(3)), entry.getValue()))
        .collect(Collectors.toList());

      final FulfilledObject object = FulfilledObject.builder()
        .identifier(new Identifier(map.get("area"), map.get("key")))
        .size(positiveLong(parseLong(map.get("size"))))
        .policy(Policy.ALL_POLICY)
        .timeStamp(new TimeStamp(OffsetDateTime.parse(map.get("created")),
          OffsetDateTime.parse(map.get("updated"))))
        .tags(Tags.tags(tags))
        .binaryRepository(binaryRepository)
        .build();

      return new FileSystemObjectEntity(object);
    } catch (Exception e) {
      throw new RuntimeException();
    }
  }

  byte[] toBytes() {
    final List<String> strings = List.of(
      identifier(),
      tags(),
      size(),
      progressingSize(),
      timestamp(),
      state());

    return strings
      .stream()
      .filter(StringUtils::isNotBlank)
      .collect(Collectors.joining(LINE_SEPARATOR))
      .getBytes(StandardCharsets.UTF_8);
  }

  private String identifier() {
    final Identifier identifier = object.getIdentifier();
    return format("area:%s", identifier.getArea())
      + LINE_SEPARATOR
      + format("key:%s", identifier.getKey());
  }

  private String tags() {
    return object.getTags().toMap()
      .entrySet()
      .stream()
      .map(entry -> format("tag-%s:%s", entry.getKey(), entry.getValue()))
      .collect(Collectors.joining(LINE_SEPARATOR));
  }

  private String timestamp() {
    return format("created:%s", object.getCreated())
      + LINE_SEPARATOR
      + format("updated:%s,", object.getUpdated());
  }

  private String state() {
    final State state = object.getSnapshot().computeState();
    return format("state:%s", state);
  }

  private String size() {
    final Snapshot snapshot = object.getSnapshot();
    return format("size:%s", snapshot.getSize());
  }

  private String progressingSize() {
    final Snapshot snapshot = object.getSnapshot();
    return format("progressingSize:%s", snapshot.getProgressingSize());
  }
}
