package io.lette1394.mediaserver.domain.storage.object;

import static io.lette1394.mediaserver.domain.storage.object.ObjectDownloadPolicy.REJECT_PENDING_OBJECT;
import static io.lette1394.mediaserver.domain.storage.object.UploadPolicy.ALLOW_UNDER_10MB_SIZE;
import static io.lette1394.mediaserver.domain.storage.object.UploadPolicy.REJECT_RESUME_UPLOAD;

import io.lette1394.mediaserver.common.Result;
import io.lette1394.mediaserver.domain.storage.object.UploadPolicy.ObjectUploadPolicy;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface ObjectLifecyclePolicy {
  ObjectLifecyclePolicy ALL_LIFECYCLE_POLICY = new ObjectLifecyclePolicy() {
    @Override
    public CompletableFuture<Result> beforeUploading(Object object,
      BinaryRepository binaryRepository) {
      return BiTestable.AllMatch.allMatch(Set.of(

        REJECT_RESUME_UPLOAD

      )).test(object, binaryRepository);
    }

    @Override
    public CompletableFuture<Result> afterUploaded(Object object,
      BinaryRepository binaryRepository) {
      return ObjectUploadPolicy.AllMatch.allMatch(Set.of(

        ALLOW_UNDER_10MB_SIZE

      )).test(object, binaryRepository);
    }

    @Override
    public CompletableFuture<Result> beforeDownloading(Object object) {
      return ObjectDownloadPolicy.AllMatch.allMatch(Set.of(

        REJECT_PENDING_OBJECT

      )).test(object);
    }
  };

  CompletableFuture<Result> beforeUploading(Object object,
    BinaryRepository binaryRepository);

  CompletableFuture<Result> afterUploaded(Object object,
    BinaryRepository binaryRepository);

  CompletableFuture<Result> beforeDownloading(Object object);
}
