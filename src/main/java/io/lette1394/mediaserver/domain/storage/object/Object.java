package io.lette1394.mediaserver.domain.storage.object;

import io.lette1394.mediaserver.domain.storage.usecase.StorageResult;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

// TODO: entity 표현
public class Object {
  // TODO: getter를 없앨 수는 없나?
  @Getter private final Identifier identifier;
  private final Attributes attributes;

  private final Storage storage;

  @Builder(access = AccessLevel.PACKAGE)
  public Object(Identifier identifier, Attributes attributes, Storage storage) {
    this.identifier = identifier;
    this.attributes = attributes;
    this.storage = storage;
  }

  public StorageResult<Void> upload(BinarySupplier binarySupplier) {
    return storage.upload(this, binarySupplier);
  }

  public StorageResult<BinarySupplier> download() {
    return storage.download(this);
  }
}
