package io.lette1394.mediaserver.domain.storage.usecase;

import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;


// TODO: 왠지 이 클래스가 StroageResult 가 아니라, 뭔가 sync와 async를 추상화 했다는
//  그런 느낌의 무언가로 이름이 바뀌어야 할 거 같다...
//  __
//  반론: 그게 아니고, 각 context 마다 동일한 역할을 (이름만 다른) 하는 클래스를 만들고
//   그와 관련된 정책을 각각 주입할 수 있도록 하게 만들어야 한다.

// TODO: timeout policy
@RequiredArgsConstructor(staticName = "storageResult")
public class StorageResult<T> {
  @Delegate
  private final CompletableFuture<T> future;

  public static <T> StorageResult<T> completed(T item) {
    return new StorageResult<T>(CompletableFuture.completedFuture(item));
  }

  public static StorageResult<Void> completed() {
    return new StorageResult<>(CompletableFuture.completedFuture(null));
  }

  public static <T> StorageResult<T> failed(Throwable reason) {
    return new StorageResult<T>(CompletableFuture.failedFuture(reason));
  }
}
