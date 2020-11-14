package io.lette1394.mediaserver.storage.domain;

import java.util.Optional;
import java.util.function.Function;
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
public interface BinaryPublisher<T extends Payload> extends Publisher<T> {
  static <P extends Payload> BinaryPublisher<P> adapt(Publisher<P> toAdapt) {
    return toAdapt::subscribe;
  }

  default Optional<Long> length() {
    return Optional.empty();
  }

  default Context currentContext() {
    return Context.empty();
  }

  default <R extends Payload> BinaryPublisher<R> map(Function<T, R> mapper) {
    return subscriber -> subscribe(new DelegatingSubscriber<T, R>(subscriber) {
      @Override
      public void onNext(T payload) {
        subscriber.onNext(mapper.apply(payload));
      }
    });
  }
}













