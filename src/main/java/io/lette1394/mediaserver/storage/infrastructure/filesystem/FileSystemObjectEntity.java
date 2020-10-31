package io.lette1394.mediaserver.storage.infrastructure.filesystem;

import static io.lette1394.mediaserver.common.NonBlankString.nonBlankString;
import static java.lang.Long.parseLong;
import static java.lang.String.format;

import io.lette1394.mediaserver.common.TimeStamp;
import io.lette1394.mediaserver.storage.domain.BinaryRepository;
import io.lette1394.mediaserver.storage.domain.Command;
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
import java.util.stream.Collectors;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;

@Value
public class FileSystemObjectEntity<BUFFER extends Payload> {
  private static final String LINE_SEPARATOR = "\n";
  Object<BUFFER> object;

  public static <BUFFER extends Payload> FileSystemObjectEntity<BUFFER> fromBytes(byte[] bytes, BinaryRepository<BUFFER> binaryRepository) {
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

      final Object<BUFFER> object = Object.<BUFFER>builder()
        .identifier(new Identifier(map.get("area"), map.get("key")))
        .objectPolicy(ObjectPolicy.ALL_OBJECT_POLICY)
        .timeStamp(new TimeStamp(OffsetDateTime.parse(map.get("created")),
          OffsetDateTime.parse(map.get("updated"))))
        .tags(Tags.tags(tags))
        .objectSnapshot(ObjectSnapshot.builder()
          .size(parseLong(map.get("size")))
          .objectType(ObjectType.valueOf(map.get("type")))
          .command(Command.NO_OPERATION)
          .build())
        .binaryRepository(binaryRepository)
        .build();

      return new FileSystemObjectEntity<>(object);
    } catch (Exception e) {
      throw new RuntimeException();
    }
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
      + format("updated:%s,", object.getUpdated());
  }

  private String type() {
    return format("type:%s", object.getType());
  }

  private String size() {
    return format("size:%s", object.getSize());
  }
}
