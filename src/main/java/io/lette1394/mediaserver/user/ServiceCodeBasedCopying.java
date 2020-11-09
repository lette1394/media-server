package io.lette1394.mediaserver.user;

import static java.lang.String.format;

import io.lette1394.mediaserver.storage.domain.Identifier;
import io.lette1394.mediaserver.storage.domain.Object;
import io.lette1394.mediaserver.storage.domain.Payload;
import io.lette1394.mediaserver.storage.usecase.copy.Copying;
import io.lette1394.mediaserver.user.Config.CopyMode;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@RequiredArgsConstructor
public class ServiceCodeBasedCopying<B extends Payload> {
  private final Copying<B> copying;
  private final ConfigRepository configRepository;

  public CompletableFuture<Object<B>> copy(Command command) {
    final Config config = configRepository.get(command.fromServiceCode, command.fromSpaceId);

    Copying.CopyMode copyMode;
    if (config.getCopyMode() == CopyMode.HARD) {
      copyMode = Copying.CopyMode.HARD;
    } else {
      copyMode = Copying.CopyMode.SOFT;
    }

    return copying.copy(Copying.Command.builder()
      .from(new Identifier(
        format("%s/%s", command.fromServiceCode, command.fromSpaceId),
        command.fromOid))
      .to(new Identifier(
        format("%s/%s", command.toServiceCode, command.toSpaceId),
        command.toOid))
      .mode(copyMode)
      .replicatingThreshold(config.getReplicatingSoftThreshold())
      .build()
    );
  }

  @Value
  public static class Command {
    String fromServiceCode;
    String fromSpaceId;
    String fromOid;

    String toServiceCode;
    String toSpaceId;
    String toOid;
  }
}
