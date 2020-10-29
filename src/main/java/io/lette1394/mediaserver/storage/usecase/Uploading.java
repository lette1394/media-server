package io.lette1394.mediaserver.storage.usecase;

import io.lette1394.mediaserver.storage.domain.BinarySupplier;
import io.lette1394.mediaserver.storage.domain.Events.UploadRejected;
import io.lette1394.mediaserver.storage.domain.Events.UploadingTriggered;
import io.lette1394.mediaserver.storage.domain.ObjectFactory;
import io.lette1394.mediaserver.storage.domain.Identifier;
import io.lette1394.mediaserver.storage.domain.Object;
import io.lette1394.mediaserver.storage.domain.ObjectPolicy;
import io.vavr.control.Either;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import lombok.Builder;
import lombok.Value;

@Value
public class Uploading {

  public CompletableFuture<Object> upload(Command command) {
//    final ObjectFactory objectFactory = new ObjectFactory(ObjectPolicy.ALL_OBJECT_POLICY);
//    final Identifier identifier = command.identifier;
//    final Object object = objectFactory.create(identifier.getArea(), identifier.getKey());
//

    return null;
  }

  public void upload() {
    Object object;
//    Binary<String> binary = new Binary<>(null, null);
//    object.upload(binary);.


    // object를 메서드로 동작을 여러개로 해야 할까...?
    //


    // 요구사항
    // 1. resume 업로드 되는 친구/아예 안되는 친구 효율적인 제어 흐름...
    //   - resume 업로드 설정이 되어있는 친구면 이미 올라온게 있는지 찔러보고 이어서 고고
    //   - 안되어있으면 그냥 무조건 덮어쓰기
    //   - 이 때 range 업로드면?
    // 2. 업로드 트랜잭션
    // 3. 기타 각종 정책

  }

  @Value
  @Builder
  public static class Command {
    Identifier identifier;
    Map<String, String> tags;
    BinarySupplier binarySupplier;
  }
}
