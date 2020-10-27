package io.lette1394.mediaserver.storage.usecase;

import io.lette1394.mediaserver.storage.domain.Storage;
import io.lette1394.mediaserver.storage.domain.Binary;
import io.lette1394.mediaserver.storage.domain.binary.BinarySupplier;
import io.lette1394.mediaserver.storage.domain.object.Events.UploadRejected;
import io.lette1394.mediaserver.storage.domain.object.Events.UploadingTriggered;
import io.lette1394.mediaserver.storage.domain.object.Factory;
import io.lette1394.mediaserver.storage.domain.object.Identifier;
import io.lette1394.mediaserver.storage.domain.object.Object;
import io.lette1394.mediaserver.storage.domain.object.Policy;
import io.vavr.control.Either;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import lombok.Builder;
import lombok.Value;

@Value
public class Uploading {
  Storage storage;

  public CompletableFuture<Object> upload(Command command) {
    final Factory factory = new Factory(Policy.ALL_POLICY);
    final Identifier identifier = command.identifier;
    final Object object = factory.create(identifier.getArea(), identifier.getKey());

    final Either<UploadRejected, UploadingTriggered> upload = object.upload();



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
