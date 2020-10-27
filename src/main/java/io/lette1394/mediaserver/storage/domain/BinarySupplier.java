package io.lette1394.mediaserver.storage.domain;

import org.reactivestreams.Publisher;

interface BinarySupplier<BUFFER extends SizeAware> {

  Publisher<BUFFER> getAsync();
}
