package io.lette1394.mediaserver.storage.infrastructure.filesystem;

import static java.lang.Long.parseLong;
import static java.lang.String.format;

import io.lette1394.mediaserver.common.TimeStamp;
import io.lette1394.mediaserver.storage.domain.BinaryPath;
import io.lette1394.mediaserver.storage.domain.BinaryPolicy;
import io.lette1394.mediaserver.storage.domain.BinaryRepository;
import io.lette1394.mediaserver.storage.domain.BinarySnapshot;
import io.lette1394.mediaserver.storage.domain.Identifier;
import io.lette1394.mediaserver.storage.domain.Object;
import io.lette1394.mediaserver.storage.domain.ObjectPolicy;
import io.lette1394.mediaserver.storage.domain.ObjectSnapshot;
import io.lette1394.mediaserver.storage.domain.ObjectType;
import io.lette1394.mediaserver.storage.domain.Payload;
import io.lette1394.mediaserver.storage.domain.Tag;
import io.lette1394.mediaserver.storage.domain.Tags;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;

@Value
public class FileSystemObjectEntity<P extends Payload> {
  private static final String LINE_SEPARATOR = "\n";
  Object<P> object;

  public static <P extends Payload> FileSystemObjectEntity<P> fromBytes(byte[] bytes, BinaryRepository<P> binaryRepository) {
    try {
      final String raw = new String(bytes);
      final Map<String, String> map = Arrays.stream(raw.split("\n"))
        .map(line -> split(line))
        .reduce(new HashMap<>(), (acc, cur) -> {
          final String key = cur[0];
          final String value = cur[1];
          acc.put(key, value);
          return acc;
        }, (stringStringHashMap, stringStringHashMap2) -> {
          throw new RuntimeException();
        });

      final Set<Tag> tags = map.entrySet().stream()
        .filter(entry -> entry.getKey().startsWith("tag-"))
        .map(entry -> new Tag(entry.getKey().substring(4), entry.getValue()))
        .collect(Collectors.toSet());

      // TODO: use object factory
      final Object<P> object = Object.<P>builder()
        .identifier(new Identifier(map.get("area"), map.get("key")))
        .objectPolicy(ObjectPolicy.ALL_OBJECT_POLICY)
        .objectSnapshot(ObjectSnapshot.byObjectType(
          ObjectType.valueOf(map.get("type")),
          Long.parseLong(map.get("size"))))
        .binaryPolicy(BinaryPolicy.ALL_BINARY_POLICY)
        .binaryRepository(binaryRepository)
        .binarySnapshot(BinarySnapshot.initial())
        .timeStamp(new TimeStamp(OffsetDateTime.parse(map.get("created")),
          OffsetDateTime.parse(map.get("updated"))))
        .tags(Tags.tags(tags))
        .binaryPath(BinaryPath.from(new Identifier(map.get("area"), map.get("key"))))
        .build();

      return new FileSystemObjectEntity<>(object);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static String[] split(String line) {
    final int i = line.indexOf(":");
    if (i == -1) {
      throw new RuntimeException();
    }

    return new String[] {line.substring(0, i), line.substring(i+1)};
  }

  byte[] toBytes() {
    final List<String> strings = List.of(
      identifier(),
      tags(),
      size(),
      timestamp(),
      type());

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
      + format("updated:%s", object.getUpdated());
  }

  private String type() {
    return format("type:%s", object.getObjectType());
  }

  private String size() {
    return format("size:%s", object.getSize());
  }
}
