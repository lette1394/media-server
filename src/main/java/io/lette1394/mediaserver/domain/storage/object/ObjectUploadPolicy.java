package io.lette1394.mediaserver.domain.storage.object;

import java.util.function.Predicate;

public interface ObjectUploadPolicy extends Predicate<Object> {
  ObjectUploadPolicy ALLOW_RESUME_UPLOAD = object -> true;
}
