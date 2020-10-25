package io.lette1394.mediaserver.storage.domain;

import io.lette1394.mediaserver.storage.domain.binary.BinaryRepository;
import io.lette1394.mediaserver.storage.domain.object.ObjectRepository;

public interface Storage extends ObjectRepository, BinaryRepository {
}
