package io.lette1394.mediaserver.processing.domain;

import io.lette1394.mediaserver.storage.domain.BinaryPublisher;
import io.lette1394.mediaserver.storage.domain.DelegatingBinaryPublisher;
import io.lette1394.mediaserver.storage.domain.DelegatingSubscriber;
import io.lette1394.mediaserver.storage.domain.Payload;
import lombok.SneakyThrows;
import org.reactivestreams.Publisher;

// TODO: 이걸 publisher 로 하는게 맞나?
//  객체 생성 시점에 binary publisher 를 알 수가 없는데...
//  이건 필요한 건 맞는거 같은데
//  실제로 usecase에서 사용하는 거는 별도 인터페이스가 필요해보임
public class MediaAwareBinaryPublisher<B extends Payload> extends
  DelegatingBinaryPublisher<B> {
  private final MediaDecoder<B> mediaDecoder;

  public MediaAwareBinaryPublisher(
    BinaryPublisher<B> delegate,
    MediaDecoder<B> mediaDecoder) {

    super(delegate);
    this.mediaDecoder = mediaDecoder;
  }

  @SneakyThrows
  public Publisher<B> publisher() {
//    return subscriber -> delegate.publisher().subscribe(new DelegatingSubscriber<>(subscriber) {
//      @Override
//      public void onNext(B payload) {
//        mediaDecoder.appendNext(payload);
//        super.onNext(payload);
//      }
//
//      @Override
//      public void onComplete() {
//        mediaDecoder.appendCompleted();
//        super.onComplete();
//      }
    return null;
  }
}
