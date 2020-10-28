package io.lette1394.mediaserver.storage2.usecase;

import io.lette1394.mediaserver.storage2.domain.BinaryRepository;
import io.lette1394.mediaserver.storage2.domain.BinarySupplier;
import io.lette1394.mediaserver.storage2.domain.Object;
import io.lette1394.mediaserver.storage2.domain.ObjectNotFoundException;
import io.lette1394.mediaserver.storage2.domain.ObjectPath;
import io.lette1394.mediaserver.storage2.domain.ObjectRepository;
import io.lette1394.mediaserver.storage2.domain.ObjectType;
import io.lette1394.mediaserver.storage2.domain.SizeAware;

import static io.lette1394.mediaserver.storage2.domain.ObjectType.FULFILLED;
import static io.lette1394.mediaserver.storage2.domain.ObjectType.PENDING;
import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;
import static io.vavr.Predicates.instanceOf;
import static java.util.Objects.isNull;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.reactivestreams.Publisher;

@RequiredArgsConstructor
public class Uploading<BUFFER extends SizeAware> {

  private final ObjectRepository<BUFFER> objectRepository;
  private final BinaryRepository<BUFFER> binaryRepository;


  public CompletableFuture<Void> upload(Command<BUFFER> command) {
    return objectRepository
      .find(command.objectPath)
      .handle(dispatch(command))
      .thenCompose(__ -> __);
  }

  private BiFunction<Object<BUFFER>, Throwable, CompletableFuture<Void>> dispatch(
    Command<BUFFER> command) {
    return (object, e) -> {
      if (isNull(e)) {
        Match(object).of(
          Case($(is(FULFILLED)), () -> this.overwrite(object, command.publisher)),
          Case($(is(PENDING)), () -> this.append(object, command.publisher)));
      }
      return Match(e).of(
        Case($(instanceOf(ObjectNotFoundException.class)),
          () -> this.create(command.objectPath, command.publisher)),
        Case($(), () -> this.abortUpload(e)));
    };
  }

  private Predicate<Object<?>> is(ObjectType objectType) {
    return object -> object.is(objectType);
  }

  private CompletableFuture<Void> append(Object<BUFFER> object, Publisher<BUFFER> publisher) {
    final BinarySupplier<BUFFER> binary = object.upload(publisher);
    return binaryRepository.append(object.getBinaryPath(), binary);
  }

  private CompletableFuture<Void> create(ObjectPath objectPath, Publisher<BUFFER> publisher) {
    final Object<BUFFER> object = null; // TODO: object factory
    final BinarySupplier<BUFFER> binary = object.upload(publisher);

    return binaryRepository.save(object.getBinaryPath(), binary);
  }

  private CompletableFuture<Void> overwrite(Object<BUFFER> object, Publisher<BUFFER> publisher) {
    final BinarySupplier<BUFFER> binary = object.upload(publisher);
    return binaryRepository.save(object.getBinaryPath(), binary);
  }

  private CompletableFuture<Void> abortUpload(Throwable e) {
    return CompletableFuture.failedFuture(e);
  }

  @Value
  @Builder
  public static class Command<BUFFER extends SizeAware> {

    ObjectPath objectPath;
    Publisher<BUFFER> publisher;
  }
}
