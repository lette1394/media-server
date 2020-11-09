package io.lette1394.mediaserver.storage.usecase.copy;

import io.lette1394.mediaserver.storage.domain.Identifier;
import io.lette1394.mediaserver.storage.domain.Object;
import io.lette1394.mediaserver.storage.domain.Payload;
import java.util.concurrent.CompletableFuture;


// TODO: 이거 음... 인터페이스가 사용하기에 좀 적절하지 않다.
//  단순히 execute만 있어야 할 거 같은데... 조건문은 어디에 넣지?
//  이거 matches() 가 아니라, pattern matching 으로 해야할 거 같다

// TODO: 근데 그러면
//  각 CopyStrategy 에서 사용하는 tag key를
//  copying class가 알아야 하는데... 그건 적절하지 않다
//  어떻게 하지?
//  -- 일단 당장 생각나는 방법으로는 chaning이 있다. 내가 못하면 다음 객체한테 하라는 식...
//  -- 근데 이건 좀 객체 합성하기가 까다로운데...
//  테스트 관점에서는 어떤가? 
public interface CopyStrategy<BUFFER extends Payload> {
  CompletableFuture<Object<BUFFER>> execute(Object<BUFFER> sourceObject, Identifier targetIdentifier);
}
