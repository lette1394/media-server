package io.lette1394.mediaserver.storage.domain;

import org.reactivestreams.Publisher;

interface BinarySupplier<T extends SizeAware> {

  Publisher<T> getAsync();
}
