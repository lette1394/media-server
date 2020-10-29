package io.lette1394.mediaserver.storage2.domain;

import org.reactivestreams.Publisher;

// TODO: rename: `Binary` 라는 이름이 있기 때문에 이제 이거는 다른 이름이 되어야 한다...
//  Payload 정도는 어떨까?
public interface BinarySupplier<BUFFER extends SizeAware> {

  Publisher<BUFFER> getAsync();
}
