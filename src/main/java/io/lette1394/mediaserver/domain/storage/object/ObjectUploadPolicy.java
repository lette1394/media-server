package io.lette1394.mediaserver.domain.storage.object;

import java.util.Set;
import lombok.Value;

@FunctionalInterface
public interface ObjectUploadPolicy {

  ObjectUploadPolicy ALLOW_RESUME_UPLOAD = (object, storage) -> false;

  boolean test(Object object, Storage storage);

  @Value
  class AllMatch implements ObjectUploadPolicy {

    Set<ObjectUploadPolicy> policies;

    @Override
    public boolean test(Object object, Storage storage) {
      return policies.stream().allMatch(policy -> policy.test(object, storage));
    }
  }
}
