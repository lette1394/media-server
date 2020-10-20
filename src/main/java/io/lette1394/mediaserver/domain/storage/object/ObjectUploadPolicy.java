package io.lette1394.mediaserver.domain.storage.object;

import static java.util.concurrent.CompletableFuture.completedFuture;

import io.lette1394.mediaserver.common.Event;
import io.lette1394.mediaserver.common.Result;
import io.lette1394.mediaserver.domain.storage.object.ObjectEvents.Uploaded;
import io.lette1394.mediaserver.domain.storage.object.ObjectEvents.UploadingTriggered;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import lombok.Value;

@FunctionalInterface
public interface ObjectUploadPolicy {

  CompletableFuture<Result> test(Object object, BinaryRepository binaryRepository);


  ObjectUploadPolicy REJECT_RESUME_UPLOAD = (object, binaryRepository) -> binaryRepository
    .binaryExists(object)
    .thenApply(isExist -> {
      if (isExist && object.isPending()) {
        return Result.fail(new ObjectPolicyViolationException("reject resume upload"));
      }
      return Result.succeed();
    });

  ObjectUploadPolicy ALLOW_UNDER_10MB_SIZE = (object, storage) -> {
    if (object.getSize() > 1024 * 1024 * 10) {
      return completedFuture(Result.fail(new ObjectPolicyViolationException("Allow under 10MB")));
    }
    return completedFuture(Result.succeed());
  };


  Set<Event.Listener<?>> ALL_LISTENERS = Set.of(
    (UploadingTriggered event) -> AllMatch.allMatch(REJECT_RESUME_UPLOAD).test(event.object, event.binaryRepository),
    (Uploaded event)           -> AllMatch.allMatch(ALLOW_UNDER_10MB_SIZE).test(event.object, event.binaryRepository)
  );



  @Value
  class AllMatch implements ObjectUploadPolicy {
    Set<ObjectUploadPolicy> policies;

    public static AllMatch allMatch(ObjectUploadPolicy... policies) {
      return new AllMatch(Set.of(policies));
    }

    @Override
    public CompletableFuture<Result> test(Object object, BinaryRepository binaryRepository) {
      return policies
        .stream()
        .map(policy -> policy.test(object, binaryRepository))
        .reduce(
          completedFuture(Result.succeed()),
          Policies.mergeAllMatch());
    }
  }
}
