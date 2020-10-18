package io.lette1394.mediaserver.domain.storage.object;

import java.util.function.Predicate;

public interface ObjectUploadPolicy {
  boolean test(Object object, Storage storage);

  ObjectUploadPolicy ALLOW_RESUME_UPLOAD = (object, storage) -> true;
}
