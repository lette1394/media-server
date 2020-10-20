package io.lette1394.mediaserver.domain.storage.object;

import static io.lette1394.mediaserver.domain.storage.object.ObjectDownloadPolicy.REJECT_PENDING_OBJECT;
import static io.lette1394.mediaserver.domain.storage.object.ObjectUploadPolicy.ALLOW_UNDER_10MB_SIZE;
import static io.lette1394.mediaserver.domain.storage.object.ObjectUploadPolicy.REJECT_RESUME_UPLOAD;

import io.lette1394.mediaserver.common.Result;
import java.util.concurrent.CompletableFuture;

public interface ObjectLifecyclePolicy {
  ObjectLifecyclePolicy ALL_LIFECYCLE_POLICY = new ObjectLifecyclePolicy() {
    @Override
    public CompletableFuture<Result> beforeUploading(Object object,
      // TODO: 이 시점에서는 완전한 Object를 줘서는 안된다. (size등이 없을 수 있음)
      BinaryRepository binaryRepository) {
      return ObjectUploadPolicy.AllMatch.allMatch(
        REJECT_RESUME_UPLOAD
      ).test(object, binaryRepository);
    }

    @Override
    public CompletableFuture<Result> afterUploaded(Object object,
      BinaryRepository binaryRepository) {
      return ObjectUploadPolicy.AllMatch.allMatch(
        ALLOW_UNDER_10MB_SIZE
      ).test(object, binaryRepository);
    }

    @Override
    public CompletableFuture<Result> beforeDownloading(Object object) {
      return ObjectDownloadPolicy.AllMatch.allMatch(
        REJECT_PENDING_OBJECT
      ).test(object);
    }
  };

  CompletableFuture<Result> beforeUploading(Object object, BinaryRepository binaryRepository);

  CompletableFuture<Result> afterUploaded(Object object, BinaryRepository binaryRepository);

  CompletableFuture<Result> beforeDownloading(Object object);
}
