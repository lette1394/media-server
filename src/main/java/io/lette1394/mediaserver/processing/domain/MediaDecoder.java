package io.lette1394.mediaserver.processing.domain;

import io.lette1394.mediaserver.storage.domain.Payload;

// TODO: MediaDecoder 에서는 image/video/audio/file 등
//  여러 타입이 서로 다른 decode 결과를 가지므로 (속성 등, file은 width, height 등이 없다)
//  Map 등으로 runtime에 resolving 할 수 있는 결과를 return 하고
//  각 타입 별로 translate (from map -> to tag) 할 수 있는 translator를
//  processing usecase에서 사용해야 할 것이다.
//  .
//  그렇게 object tag에 넣어주기
//  tag 는 timeout 가능한 completable future 등을 받아서 (아니면 preemptive 성격의 future)
//  object 에서 tag를 저장한다
//  이렇게 atomic 한 원자성을 달성한다
//  post-process 는 storage layer 에서 할만한 성격이 아니다.
//  .
//  tag는 변경될 수 있으나 별도 usecase에서 처리되어야 하는 거고,
//  하나의 usecase에서는 업로드가 끝난 뒤에 tag를 업데이트 하는 방식은
//  있을 수 없다.

// TODO: FailedAtMaxByteSizeMediaDecoder
//  FailedByTimeoutMediaDecoder
//  SingleThreadFailedByTimeoutMediaDecoder
public interface MediaDecoder<B extends Payload> {
  // TODO: 일단 이 메서드를 구현하는 것 부터 해보자.
  //  local - apache tika 를 사용해서,
  //  remote - 뭐... 음... 뭘로 해야할 지는 모르겠지만 test는 가능할 것이다

  void appendNext(B payload);

  void appendCompleted();

  void tryDecode();

  interface Listener {
    default void beforeDecodingStarted() {
    }

    default void afterDecoded(DecodedMetadata decodedMetadata) {
    }

    default void afterDecodeFailed(Throwable throwable) {
    }
  }
}
