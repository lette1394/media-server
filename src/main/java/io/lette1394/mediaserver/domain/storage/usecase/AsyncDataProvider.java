package io.lette1394.mediaserver.domain.storage.usecase;

import java.nio.ByteBuffer;
import java.util.concurrent.Flow.Publisher;

interface AsyncDataProvider extends Publisher<ByteBuffer> {
}
