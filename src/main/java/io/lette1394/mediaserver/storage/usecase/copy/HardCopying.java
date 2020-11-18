package io.lette1394.mediaserver.storage.usecase.copy;

import io.lette1394.mediaserver.storage.domain.BinaryPath;
import io.lette1394.mediaserver.storage.domain.BinaryRepository;
import io.lette1394.mediaserver.storage.domain.Identifier;
import io.lette1394.mediaserver.storage.domain.Object;
import io.lette1394.mediaserver.storage.domain.ObjectFactory;
import io.lette1394.mediaserver.storage.domain.ObjectRepository;
import io.lette1394.mediaserver.storage.domain.Payload;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HardCopying<BUFFER extends Payload> implements CopyStrategy<BUFFER> {
  private final ObjectFactory<BUFFER> objectFactory;
  private final ObjectRepository<BUFFER> objectRepository;
  private final BinaryRepository<BUFFER> binaryRepository;

  @Override
  public CompletableFuture<Object<BUFFER>> execute(
    Object<BUFFER> sourceObject,
    Identifier targetIdentifier) {

    final Object<BUFFER> targetObject = objectFactory.create(targetIdentifier);
    return sourceObject.download()
      .thenCompose(sourceBinary -> targetObject.copyFrom(sourceBinary)) // TODO: source object 를 받기
      // TODO: 이거 병렬 실행으로 할 수 있을 거 같다
      .thenCompose(copiedObject -> objectRepository.save(copiedObject));

      // TODO: 예외 발생하면 둘 다 삭제
  }
}
