package io.lette1394.mediaserver.storage.domain;

import static io.lette1394.mediaserver.storage.domain.BinaryLifecycle.AFTER_TRANSFERRED;
import static io.lette1394.mediaserver.storage.domain.BinaryLifecycle.BEFORE_TRANSFER;
import static io.lette1394.mediaserver.storage.domain.BinaryLifecycle.DURING_TRANSFERRING;
import static io.lette1394.mediaserver.storage.domain.BinaryLifecycle.TRANSFER_ABORTED;
import static io.lette1394.mediaserver.storage.domain.Command.COPY;
import static io.lette1394.mediaserver.storage.domain.Command.DOWNLOAD;
import static io.lette1394.mediaserver.storage.domain.Command.UPLOAD;

import io.lette1394.mediaserver.common.AggregateRoot;
import io.lette1394.mediaserver.common.TimeStamp;
import io.lette1394.mediaserver.storage.domain.Events.CopyRejected;
import io.lette1394.mediaserver.storage.domain.Events.CopyingTriggered;
import io.lette1394.mediaserver.storage.domain.Events.DownloadRejected;
import io.lette1394.mediaserver.storage.domain.Events.DownloadingTriggered;
import io.lette1394.mediaserver.storage.domain.Events.UploadRejected;
import io.lette1394.mediaserver.storage.domain.Events.Uploaded;
import io.lette1394.mediaserver.storage.domain.Events.UploadingTriggered;
import io.vavr.control.Try;
import java.util.concurrent.CompletableFuture;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Delegate;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

@EqualsAndHashCode(of = "identifier", callSuper = false)
public class Object<P extends Payload> extends AggregateRoot {

  @Getter
  private final Identifier identifier;
  private final BinaryPath binaryPath;
  private final ObjectPolicy objectPolicy;
  @Delegate
  private final ObjectSnapshot objectSnapshot;
  private final Tags tags;
  @Delegate
  private final TimeStamp timeStamp;
  private final BinaryPolicy binaryPolicy;
  private final BinarySnapshot binarySnapshot;
  private final BinaryRepository<P> binaryRepository;

  @Builder
  public Object(Identifier identifier,
    BinaryPath binaryPath, ObjectPolicy objectPolicy,
    ObjectSnapshot objectSnapshot,
    Tags tags,
    TimeStamp timeStamp,
    BinaryPolicy binaryPolicy,
    BinarySnapshot binarySnapshot,
    BinaryRepository<P> binaryRepository) {

    this.identifier = identifier;
    this.binaryPath = binaryPath;
    this.objectPolicy = objectPolicy;
    this.objectSnapshot = objectSnapshot;
    this.tags = tags;
    this.timeStamp = timeStamp;
    this.binaryPolicy = binaryPolicy;
    this.binarySnapshot = binarySnapshot;
    this.binaryRepository = binaryRepository;
  }

  public BinaryPublisher<P> upload(BinaryPublisher<P> upstream) {
    return objectPolicy.test(objectSnapshot.update(UPLOAD))
      .onSuccess(__ -> addEvent(UploadingTriggered.uploadingTriggered()))
      .onFailure(e -> addEvent(UploadRejected.uploadRejected(e)))
      .map(__ -> compose(upstream))
      .getOrElseThrow(e -> new OperationCanceledException(UPLOAD, e));
  }

  public CompletableFuture<BinaryPublisher<P>> download() {
    return objectPolicy.test(objectSnapshot.update(DOWNLOAD))
      .onSuccess(__ -> addEvent(DownloadingTriggered.downloadingTriggered()))
      .onFailure(e -> addEvent(DownloadRejected.downloadRejected(e)))
      .toCompletableFuture()
      .thenCompose(__ -> binaryRepository.find(binaryPath))
      .exceptionally(e -> {
        throw new OperationCanceledException(DOWNLOAD, e);
      });
  }

  public BinaryPublisher<P> copyFrom(BinaryPublisher<P> upstream) {
    return objectPolicy.test(objectSnapshot.update(COPY))
      .onSuccess(__ -> addEvent(CopyingTriggered.copyingTriggered()))
      .onFailure(e -> addEvent(CopyRejected.copyRejected(e)))
      .map(__ -> upload(upstream))
      .getOrElseThrow(e -> new OperationCanceledException(COPY, e));
  }

  public boolean hasTag(String key) {
    return tags.has(key);
  }

  public Tag getTag(String key) {
    return tags.get(key);
  }

  public Tags getTags() {
    return tags;
  }

  public void addTag(String key) {
    tags.addTag(key);
  }

  public void addTag(String key, long value) {
    tags.addTag(key, String.valueOf(value));
  }

  public void addTag(String key, String value) {
    tags.addTag(key, value);
  }

  public Object<P> with(BinaryPath binaryPath) {
    return Object.<P>builder()
      .binaryPath(binaryPath)
      .identifier(identifier)
      .objectSnapshot(objectSnapshot)
      .binaryPolicy(binaryPolicy)
      .binaryRepository(binaryRepository)
      .binarySnapshot(binarySnapshot)
      .tags(tags)
      .objectPolicy(objectPolicy)
      .timeStamp(timeStamp)
      .build();
  }

  private BinaryPublisher<P> compose(BinaryPublisher<P> binaryPublisher) {
    return new DelegatingBinaryPublisher<>(binaryPublisher) {
      @Override
      public void subscribe(Subscriber<? super P> subscriber) {
        super.length()
          .map(length -> listenable(controllable(lengthValidatable(length, delegate))))
          .orElseGet(() -> listenable(controllable(delegate)))
          .subscribe(subscriber);
      }
    };
  }

  private Publisher<P> lengthValidatable(long length, Publisher<P> publisher) {
    return new ValidatingLengthPublisher<>(length, publisher);
  }

  private Publisher<P> controllable(Publisher<P> publisher) {
    return new ControllablePublisher<>(policy(), publisher);
  }

  private Publisher<P> listenable(Publisher<P> publisher) {
    return new ListenablePublisher<>(listener(), publisher);
  }

  private ControllablePublisher.Policy policy() {
    return new ControllablePublisher.Policy() {
      @Override
      public Try<Void> beforeTransfer() {
        return binaryPolicy.test(
          binarySnapshot.update(BEFORE_TRANSFER));
      }

      @Override
      public Try<Void> duringTransferring(long currentLength) {
        return binaryPolicy.test(
          binarySnapshot
            .update(currentLength)
            .update(DURING_TRANSFERRING));
      }

      @Override
      public Try<Void> afterTransferred(long totalLength) {
        return binaryPolicy.test(
          binarySnapshot
            .update(totalLength)
            .update(AFTER_TRANSFERRED));
      }
    };
  }

  private ListenablePublisher.Listener listener() {
    return new ListenablePublisher.Listener() {
      private boolean aborted = false;

      @Override
      public void beforeTransfer() {
        binarySnapshot.update(BEFORE_TRANSFER);
      }

      @Override
      public void duringTransferring(long currentLength) {
        binarySnapshot
          .update(currentLength)
          .update(DURING_TRANSFERRING);
        objectSnapshot
          .update(currentLength);
      }

      @Override
      public void afterTransferred(long totalLength) {
        binarySnapshot
          .update(totalLength)
          .update(AFTER_TRANSFERRED);
        objectSnapshot
          .update(totalLength)
          .update(ObjectType.FULFILLED);
        addEvent(Uploaded.uploaded()); // TODO: command 별 분기
      }

      @Override
      public void transferAborted(Throwable throwable) {
        if (aborted) {
          return;
        }
        aborted = true;
        binarySnapshot.update(TRANSFER_ABORTED);
        objectSnapshot.update(ObjectType.PENDING);
        addEvent(Events.UploadAborted.uploadAborted(throwable));
      }
    };
  }
}
