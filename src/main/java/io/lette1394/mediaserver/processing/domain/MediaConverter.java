package io.lette1394.mediaserver.processing.domain;

import java.util.concurrent.CompletableFuture;


public interface MediaConverter {

  // TODO:
  //  자 여기서...
  //  과연 parameter를 뭘로 해야 할 것인가
  //  1. BinaryPublisher
  //  2. File (이건 아닌 거 같다)
  //  3. URL / URI
  //  4. Identifier
  //  5. BinaryPath
  //  .
  //  .
  //  .
  //  media converting 되는 구조를 생각해보면
  //  특정 local/private network 안에서 converter와 storage 가 같이 존재한다
  //  converter는 파일을 빠르게 가져와야 하고
  //  또 연산 결과를 storage에 써야 하기 때문에
  //  근데 뭐 반드시 그럴 필요는 없는거고 그게 특정만 되면 된다 어디에 뭐가 있는지..
  //  .
  //  두 가지 요구사항이 있을려나?
  //  1. upload 와 동시에 인코딩 하기
  //  2. upload 끝나고 인코딩 하기 (안 할 수도 있고)
  //  ------
  //  1번의 경우에 프로세스는
  //  효율적: 나한테도 upload converter한테도 upload -> 업로드 완료와 동시에 인코딩 시작 -> 완료 후 저장
  //  비효율: 업로드 완료 -> converter한테 다운받아가라고 한다 -> 다운받고 인코딩 시작 -> 완료 후 저장
  //  이러면 Object가 생성되기도 전이기 때문에 BinaryPUblisher 를 줘야하는게 맞다.
  //  대신에 upload가 취소되거나 에러가 나는 경우에 처리가 복잡함
  //  .
  //  2번은 당연히 Identifier다
  //  object가 생성되어 있을 거니까... 그것만 던져주면 내부에서 BinaryPath를 만들 수 있기 때문.
  //  BinaryPath를 직접 주지 않는 이유는, 당연히 Object 정책을 태워야 하고 /
  //  단순히 BianryPath를 convert 한다는 말이 안되기 때문!
  //  .
  //  ~~이거 근데, initial object에 대해~~
  //  ~~object.upload() 하고나서 바로 object.download()를 호출할 수 있는 형태라면~~
  //  ~~parameter를 Object만 받아도 될 거 같다.~~
  //  .
  //  근데 그럴 필요가 없지
  //  내가 upstream을 알고 있는데 뭐하러...?
  //  .
  //  .
  //  .
  //  내가 봤을 때 이거는 usecase가 나눠지는 거다.
  //  1. 그냥 업로드
  //  2. 업로드하면서 eager 하게 convert
  //  3. lazy convert
  //  세 가지의 usecase가 있는거고, flow가 각각 다름
  //  따라서 이 interface의 메서드는
  //  최소 요건인
  //  BinaryPublisher를 받아야 한다.....................................
  //  근데 아닌거같네. input source 위치를 바로 받기도 하잖아...
  //  그니까 두 개로 좁혀진건가
  //  .
  //  1. BinarySupplier
  //  2. BinaryPath



  // TODO: 자꾸 드는 생각은
  //  MediaObject 의 존재다.
  //

  CompletableFuture<Void> convert();
}
