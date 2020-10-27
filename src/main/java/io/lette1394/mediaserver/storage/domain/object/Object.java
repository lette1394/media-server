package io.lette1394.mediaserver.storage.domain.object;

import static io.lette1394.mediaserver.storage.domain.object.Command.DOWNLOAD;
import static io.lette1394.mediaserver.storage.domain.object.Command.UPLOAD;
import static io.lette1394.mediaserver.storage.domain.object.Events.DownloadRejected.*;
import static io.lette1394.mediaserver.storage.domain.object.Events.DownloadingTriggered.*;
import static io.lette1394.mediaserver.storage.domain.object.Events.UploadRejected.*;
import static io.lette1394.mediaserver.storage.domain.object.Events.UploadingTriggered.*;

import io.lette1394.mediaserver.common.AggregateRoot;
import io.lette1394.mediaserver.common.TimeStamp;
import io.lette1394.mediaserver.storage.domain.binary.Binary;
import io.lette1394.mediaserver.storage.domain.object.Events.DownloadRejected;
import io.lette1394.mediaserver.storage.domain.object.Events.DownloadingTriggered;
import io.lette1394.mediaserver.storage.domain.object.Events.UploadRejected;
import io.lette1394.mediaserver.storage.domain.object.Events.UploadingTriggered;
import io.vavr.control.Either;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Delegate;

@EqualsAndHashCode(of = "identifier", callSuper = false)
public abstract class Object extends AggregateRoot {

  @Getter
  protected final Identifier identifier;
  protected final Policy policy;

  protected final Tags tags;
  @Delegate
  protected final TimeStamp timeStamp;

  protected Object(
    Identifier identifier,
    Policy policy,
    TimeStamp timeStamp,
    Tags tags) {
    this.identifier = identifier;
    this.policy = policy;
    this.timeStamp = timeStamp;
    this.tags = tags;
  }

  public Either<UploadRejected, UploadingTriggered> upload() {
    return policy.test(snapshot(UPLOAD)).toEither()
      .map(__ -> uploadingTriggered())
      .mapLeft(e -> uploadRejected(e));
  }

  public Either<DownloadRejected, DownloadingTriggered> download() {
    return policy.test(snapshot(DOWNLOAD)).toEither()
      .map(__ -> downloadingTriggered())
      .mapLeft(e -> downloadRejected(e));
  }

  private Snapshot snapshot(Command command) {
    return Snapshot.builder()
      .identifier(identifier)
      .command(command)
      .size(getSize())
      .type(getType())
      .build();
  }

  public abstract long getSize();

  public abstract Type getType();

  public Tags getTags() {
    return tags;
  }
}
