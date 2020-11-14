package io.lette1394.mediaserver.storage.domain;

import java.util.Optional;
import org.reactivestreams.Publisher;

/**
 * A binary supplier which hide whether it support sync or async way from caller.<br>
 * <br>
 *
 * <h2>Abstraction Level</h2>
 * <p>
 * Domain objects can use this {@link BinaryPublisher} interface for hiding their specific binary
 * transfer logic whether it is sync or async way. Domain object can hold their code flow without
 * any considerations of implementation details.<br>
 * <br>
 *
 * <h2>Restrictions</h2>
 *
 * @author Jae eun Na
 * @see ListenablePublisher
 * @see ControllablePublisher
 * @see DelegatingBinaryPublisher
 */

@FunctionalInterface
public interface BinaryPublisher<P extends Payload> extends Publisher<P> {
  static <P extends Payload> BinaryPublisher<P> adapt(Publisher<P> toAdapt) {
    return toAdapt::subscribe;
  }

  default Optional<Long> length() {
    return Optional.empty();
  }

  default Context currentContext() {
    return Context.empty();
  }
}













