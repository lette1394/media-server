package io.lette1394.mediaserver.storage.usecase;

import io.lette1394.mediaserver.storage.domain.BinaryPath;
import io.lette1394.mediaserver.storage.domain.BinaryRepository;
import io.lette1394.mediaserver.storage.domain.BinarySupplier;
import io.lette1394.mediaserver.storage.domain.Identifier;
import io.lette1394.mediaserver.storage.domain.Object;
import io.lette1394.mediaserver.storage.domain.ObjectFactory;
import io.lette1394.mediaserver.storage.domain.ObjectRepository;
import io.lette1394.mediaserver.storage.domain.SizeAware;
import io.lette1394.mediaserver.storage.infrastructure.Publishers;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.reactivestreams.Publisher;

@RequiredArgsConstructor
public class Uploading<BUFFER extends SizeAware> {
  private final BinaryRepository<BUFFER> binaryRepository;
  private final ObjectRepository<BUFFER> objectRepository;

  // TODO: append, overwrite 등 분기처리는 어디서...?
  //  presentation layer?
  //  아니야 이것도 결국에는 control-flow 니까, 또 다른 usecase 에서 써야해.
  //  facade class: Uploading을 만들자.
  public <T> CompletableFuture<Object<BUFFER>> upload(Command<T, BUFFER> command) {
    final ObjectFactory<BUFFER> objectFactory = new ObjectFactory<>();
    final Object<BUFFER> object = objectFactory.create(command.identifier);

    final BinarySupplier<BUFFER> binarySupplier = object
      .upload(Publishers.convert(command.upstream, command.mapper));

    return binaryRepository.create(new BinaryPath() {
      @Override
      public String asString() {
        return String.format("%s/%s", command.identifier.getArea(), command.identifier.getKey());
      }
    }, binarySupplier)
    .thenCompose(__ -> objectRepository.saveObject(object));
  }

//  public void upload() {
//    Object object;
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
//
//  }

  @Value
  @Builder
  public static class Command<T, BUFFER extends SizeAware> {

    Identifier identifier;
    Map<String, String> tags;
    Publisher<T> upstream;
    Function<T, BUFFER> mapper; // mapper 를 usecase 에서 해야하나? 외부 presentation layer에서 받아야하는거아님?
  }
}
