package io.lette1394.mediaserver.domain.storage.object;

import io.lette1394.mediaserver.common.AbstractionBoundary;

@AbstractionBoundary
public interface Storage extends ObjectRepository, BinaryRepository {
}
