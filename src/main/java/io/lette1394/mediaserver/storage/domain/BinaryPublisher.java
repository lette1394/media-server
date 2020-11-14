package io.lette1394.mediaserver.storage.domain;

import io.lette1394.mediaserver.common.Contracts;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import reactor.core.publisher.Flux;

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

  void subscribe(Subscriber<? super P> s);

  default Optional<Long> length() {
    return Optional.empty();
  }

  default Context currentContext() {
    return Context.empty();
  }

  default <R extends Payload> BinaryPublisher<R> map(Function<P, R> mapper) {
    final BinaryPublisher<P> origin = this;
    return new BinaryPublisher<>() {
      @Override
      public void subscribe(Subscriber<? super R> subscriber) {
        origin.subscribe(new DelegatingSubscriber<P, R>(subscriber) {
          @Override
          public void onNext(P payload) {
            delegate.onNext(mapper.apply(payload));
          }
        });
      }

      @Override
      public Optional<Long> length() {
        return origin.length();
      }

      @Override
      public Context currentContext() {
        return origin.currentContext();
      }
    };
  }

  default BinaryPublisher<P> forEach(Consumer<P> consumer) {
    return new DelegatingBinaryPublisher<>(this) {
      @Override
      public void subscribe(Subscriber<? super P> subscriber) {
        delegate.subscribe(new DelegatingSubscriber<P, P>(subscriber) {
          @Override
          public void onNext(P payload) {
            consumer.accept(payload);
            subscriber.onNext(payload);
          }
        });
      }
    };
  }

  // TODO: remove Flux dependency
  default BinaryPublisher<P> broadcast(int minSubscribers) {
    Contracts.require(minSubscribers > 0, "minSubscribers > 0");
    final BinaryPublisher<P> retained = this.forEach(payload -> payload.retain(minSubscribers - 1));
    // TODO: 왜 1 빼는지
    final Publisher<P> broadcasting = Flux
      .from(retained)
      .publish()
      .autoConnect(minSubscribers);

    return new DelegatingBinaryPublisher<>(retained) {
      @Override
      public void subscribe(Subscriber<? super P> s) {
        broadcasting.subscribe(s);
      }
    };
  }
}
