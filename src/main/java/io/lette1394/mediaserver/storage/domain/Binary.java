package io.lette1394.mediaserver.storage.domain;

import static io.lette1394.mediaserver.storage.domain.Events.Uploaded.*;
import static io.lette1394.mediaserver.storage.domain.Events.UploadingAborted.*;
import static io.lette1394.mediaserver.storage.domain.object.Events.UploadingTriggered.*;

import io.lette1394.mediaserver.common.Notifiable;
import io.lette1394.mediaserver.storage.domain.ListenableBinarySupplier.Listener;
import io.vavr.control.Try;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class Binary {

  protected final Notifiable root;
  protected final BinaryPolicy policy;
  protected final BinarySupplier<? extends SizeAware> binarySupplier;
  protected final BinaryRepository binaryRepository;

  private final BinarySnapshot snapshot = BinarySnapshot.builder()
    .build();

  CompletableFuture<Void> upload(BinaryPath path) {
    return binaryRepository.save(path, binarySupplier);
  }

  public CompletableFuture<Void> upload(BinarySupplier<? extends SizeAware> binarySupplier) {
    return checkBeforeUpload().toCompletableFuture()
      .thenCompose(__ -> doUpload(wrap(binarySupplier)))
      .thenCompose(__1 ->
        checkAfterUploaded(snapshot.getProgressingSize())
          .toCompletableFuture());
  }

  // TODO: rename
  protected abstract CompletableFuture<Void> doUpload(BinarySupplier<? extends SizeAware> binarySupplier);

  private Try<Void> checkBeforeUpload() {
    root.notify(uploadingTriggered());
    return policy
      .test(snapshot.update(LifeCycle.BEFORE_UPLOAD).update(0L))
      .onFailure(this::abortUpload);
  }

  private Try<Void> checkDuringUploading(long currentSize) {
    // TODO: event 발행 -> 성능 문제가 있을 거 같은데...
    return policy
      .test(snapshot.update(LifeCycle.DURING_UPLOADING).update(currentSize))
      .onFailure(this::abortUpload);
  }

  private Try<Void> checkAfterUploaded(long totalSize) {
    root.notify(uploaded());
    return policy
      .test(snapshot.update(LifeCycle.AFTER_UPLOADED).update(totalSize))
      .onFailure(this::abortUpload);
  }


  // TODO: exception handler를 위해 event listener 가 또 필요할까...?
  //  domain event 만으로 충분히 가능할 거 같은데... 안되려나
  //  한 번 해 보자.
  private void abortUpload(Throwable throwable) {
    root.notify(uploadingAborted(throwable));
  }

  private BinarySupplier<? extends SizeAware> wrap(BinarySupplier<? extends SizeAware> binarySupplier) {
    final BinarySupplier<? extends SizeAware> listenableBinarySupplier = new ListenableBinarySupplier<>(
      binarySupplier, new Listener() {
      //TODO: 클래스로 빼자
      private boolean aborted = false;

      @Override
      public void transferAborted(Throwable throwable) {
        if (aborted) {
          return;
        }
        abortUpload(throwable);
        aborted = true;
      }

      @Override
      public void duringTransferring(long currentLength) {
        snapshot.update(currentLength);
      }

      @Override
      public void afterTransferred(long totalLength) {
        snapshot.update(totalLength);
      }
    });

    return new ControllableBinarySupplier<>(
      listenableBinarySupplier, new ControllableBinarySupplier.Policy() {
      @Override
      public Try<Void> duringTransferring(long currentLength) {
        return checkDuringUploading(currentLength);
      }
    });
  }
}
