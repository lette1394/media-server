package io.lette1394.mediaserver.storage.domain;

import static io.lette1394.mediaserver.common.CompletableFutures.unwrap;
import static io.lette1394.mediaserver.storage.domain.BinaryLifecycle.AFTER_TRANSFERRED;
import static io.lette1394.mediaserver.storage.domain.BinaryLifecycle.BEFORE_TRANSFER;
import static io.lette1394.mediaserver.storage.domain.BinaryLifecycle.DURING_TRANSFERRING;
import static io.lette1394.mediaserver.storage.domain.BinaryLifecycle.TRANSFER_ABORTED;
import static io.lette1394.mediaserver.storage.domain.Command.COPY;
import static io.lette1394.mediaserver.storage.domain.Command.DOWNLOAD;
import static io.lette1394.mediaserver.storage.domain.Command.UPLOAD;
import static io.lette1394.mediaserver.storage.domain.ObjectType.FULFILLED;
import static io.lette1394.mediaserver.storage.domain.ObjectType.INITIAL;
import static io.lette1394.mediaserver.storage.domain.ObjectType.PENDING;
import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;

import io.lette1394.mediaserver.common.AggregateRoot;
import io.lette1394.mediaserver.common.Event;
import io.lette1394.mediaserver.common.TimeStamp;
import io.lette1394.mediaserver.storage.domain.Events.Copied;
import io.lette1394.mediaserver.storage.domain.Events.CopyRejected;
import io.lette1394.mediaserver.storage.domain.Events.CopyingTriggered;
import io.lette1394.mediaserver.storage.domain.Events.DownloadRejected;
import io.lette1394.mediaserver.storage.domain.Events.Downloaded;
import io.lette1394.mediaserver.storage.domain.Events.DownloadingTriggered;
import io.lette1394.mediaserver.storage.domain.Events.UploadRejected;
import io.lette1394.mediaserver.storage.domain.Events.Uploaded;
import io.lette1394.mediaserver.storage.domain.Events.UploadingTriggered;
import io.vavr.control.Try;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
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
    BinaryPath binaryPath,
    ObjectPolicy objectPolicy,
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

  // TODO: 인터페이스가 너무 이상하다... 나는 upload()를 했는데
  //  왜 return 값이 parameter랑 같아?
  //  download랑 쌍도 안맞고, 이해하기가 어렵다.
  //  지금 usecase를 보면 그대로 바로 그냥 BinaryRepository에 save하는 일만 하고 있으니,
  //  여기서 그냥 다 해주고 CompletableFuture를 반환해 주는게 낫겠다.
  public CompletableFuture<Object<P>> upload(BinaryPublisher<P> upstream) {
    return objectPolicy.test(objectSnapshot.update(UPLOAD))
      .onSuccess(__ -> addEvent(UploadingTriggered.uploadingTriggered()))
      .onFailure(e -> addEvent(UploadRejected.uploadRejected(e)))
      .map(__ -> compose(upstream))
      .toCompletableFuture()
      .thenCompose(dispatchUpload())
      .thenApply(__ -> this)
      .exceptionally(e -> {
        throw new OperationCanceledException(UPLOAD, unwrap(e));
      });
  }

  // TODO: 이거 어... 여기서 하는 게 맞나?
  //  copy랑은 또 안맞잖아... 그러면 어떻게 되는거야ㅜㅜ 젠장
  //  .
  //  .
  //  .
  //  자. 생각해보자.
  //  COPY strategy(hard, soft, replicating hard)는 확실히 usecase가 맞아.
  //  왜? invariants 가 각각 달라지니까!!
  //  .
  //  .
  //  .
  //  근데 upload는 좀 다르다.
  //  이건 usecase가 아니야.
  //  어떤 경우에는 PENDING state 일 때 upload 하는 경우가..... valid 하네?
  //  젠장. 이것도 usecase 따라 달라지는 거네.
  //  invariant가 있는데 usecase level에 있는거였다...
  //  PENDING state 라고 해서 항상 append() 하는 게 아니야.
  //  어떤 경우(이어올리기를 사용하지 않는 경우)는 PENDING state 일 때 upload를 할 수도 있어. 이거 valid 해
  //  UPLOADING 에 dispatch logic을 다시 옮겨야겠다.
  private Function<BinaryPublisher<P>, CompletableFuture<Void>> dispatchUpload() {
    return binaryPublisher -> Match(this)
      // FIXME (jaeeun) 2020-11-20:
      //  object == null 일 때 handling
      .of(
        Case($(o -> o.is(FULFILLED)), () -> binaryRepository.create(binaryPath, binaryPublisher)),
        Case($(o -> o.is(INITIAL)), () -> binaryRepository.create(binaryPath, binaryPublisher)),
        Case($(o -> o.is(PENDING)), () -> binaryRepository.append(binaryPath, binaryPublisher)));
    // TODO: 매칭 안될 때
  }

  public CompletableFuture<BinaryPublisher<P>> download() {
    return objectPolicy.test(objectSnapshot.update(DOWNLOAD))
      .onSuccess(__ -> addEvent(DownloadingTriggered.downloadingTriggered()))
      .onFailure(e -> addEvent(DownloadRejected.downloadRejected(e)))
      .toCompletableFuture()
      .thenCompose(__ -> binaryRepository.find(binaryPath))
      .thenApply(addEventStep(Downloaded.downloaded()))
      .exceptionally(e -> {
        throw new OperationCanceledException(DOWNLOAD, e);
      });
  }

  // TODO: 이것도 upload() 메서드랑 같이 이상한데...
  //  이것도 upload() 와 같이 만들려면 SoftCopying 쪽이 문젠데,
  //  SoftCopying 에 있는 objectFactory에 있는 BinaryRepository를 아무것도 안하게 만들면 해결 가능하다.
  public CompletableFuture<Object<P>> copyFrom(BinaryPublisher<P> upstream) {
    return objectPolicy.test(objectSnapshot.update(COPY))
      .onSuccess(__ -> addEvent(CopyingTriggered.copyingTriggered()))
      .onFailure(e -> addEvent(CopyRejected.copyRejected(e)))
      .toCompletableFuture()
      .thenCompose(__ -> upload(upstream))
      .thenApply(addEventStep(Copied.copied()))
      .exceptionally(e -> {
        throw new OperationCanceledException(COPY, e);
      });
  }

  private <T> Function<T, T> addEventStep(Event event) {
    return object -> {
      addEvent(event);
      return object;
    };
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

  public void addAllTag(Tags tags) {
    this.tags.addAllTag(tags);
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
