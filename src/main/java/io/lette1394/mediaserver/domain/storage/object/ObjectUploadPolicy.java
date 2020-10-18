package io.lette1394.mediaserver.domain.storage.object;

import com.google.common.base.Predicates;
import java.util.Set;
import java.util.function.Predicate;
import lombok.Value;

@FunctionalInterface
public interface ObjectUploadPolicy {
  boolean test(Object object, Storage storage);

  ObjectUploadPolicy ALLOW_RESUME_UPLOAD = (object, storage) -> false;

  @Value
  class AllMatch implements ObjectUploadPolicy {
    Set<ObjectUploadPolicy> policies;

    @Override
    public boolean test(Object object, Storage storage) {
      return policies.stream().allMatch(policy -> policy.test(object, storage));
    }
  }
}
