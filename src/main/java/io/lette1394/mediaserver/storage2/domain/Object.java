package io.lette1394.mediaserver.storage2.domain;

import io.lette1394.mediaserver.common.AggregateRoot;
import io.lette1394.mediaserver.storage.domain.Events.UploadingAborted;
import io.lette1394.mediaserver.storage.domain.object.Events.UploadingTriggered;
import lombok.Builder;
import lombok.experimental.Delegate;
import org.reactivestreams.Publisher;

public class Object<BUFFER extends SizeAware> extends AggregateRoot {

  private final ObjectPolicy objectPolicy;

  private final ObjectPath objectPath;
  private final ObjectSnapshot objectSnapshot;

  private final Binary<BUFFER> binary;
  @Delegate(excludes = Enum.class)
  private final ObjectType objectType;

  private final long size;

  @Builder
  public Object(ObjectPolicy objectPolicy,
    ObjectPath objectPath, ObjectSnapshot objectSnapshot,
    Binary<BUFFER> binary, ObjectType objectType, long size) {
    this.objectPolicy = objectPolicy;
    this.objectPath = objectPath;
    this.objectSnapshot = objectSnapshot;
    this.binary = binary;
    this.objectType = objectType;
    this.size = size;
  }

  public BinarySupplier<BUFFER> download() {
    return wrap(binary.getBinarySupplier());
  }

  public BinarySupplier<BUFFER> upload(Publisher<BUFFER> publisher) {
    return wrap(() -> publisher);
  }

  private BinarySupplier<BUFFER> wrap(BinarySupplier<BUFFER> binarySupplier) {
    return objectPolicy.test(
      ObjectSnapshot.builder()
        .size(size)
        .command(Command.UPLOAD)
        .objectType(objectType)
        .build())
      .map(__ -> binarySupplier)
      .onSuccess(__ -> addEvent(UploadingTriggered.uploadingTriggered()))
      .onFailure(e -> addEvent(UploadingAborted.uploadingAborted(e)))
      .getOrElseThrow(e -> new OperationAborted(Command.UPLOAD, e));
  }

  public BinaryPath getBinaryPath() {
    return new BinaryPath() {};
  }

  public static Object<SizeAware> create(ObjectPath objectPath, Binary<SizeAware> binary) {
    ObjectSnapshot.builder()
      .build();

    return Object.builder()
      .objectPolicy(ObjectPolicy.ALL_POLICY)
      .objectPath(objectPath)
      .objectSnapshot(ObjectSnapshot.initial())
      .binary(binary)
      .build();
  }
}
