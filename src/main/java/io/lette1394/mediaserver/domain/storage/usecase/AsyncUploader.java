package io.lette1394.mediaserver.domain.storage.usecase;

import io.lette1394.mediaserver.common.Result;
import io.lette1394.mediaserver.domain.storage.Object;
import java.util.concurrent.CompletableFuture;

interface AsyncUploader extends Uploader {

    CompletableFuture<Result> uploadAsync(Object object);

    @Override
    default Result upload(Object object) {
        return uploadAsync(object).join();
    }
}
