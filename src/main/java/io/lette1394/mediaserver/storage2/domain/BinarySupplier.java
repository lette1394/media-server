package io.lette1394.mediaserver.storage2.domain;

import org.reactivestreams.Publisher;

public interface BinarySupplier<BUFFER extends SizeAware> {

  Publisher<BUFFER> getAsync();
}
