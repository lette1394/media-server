package io.lette1394.mediaserver.domain.storage.usecase;

import io.lette1394.mediaserver.common.Result;
import io.lette1394.mediaserver.domain.storage.Object;
import java.util.concurrent.CompletableFuture;
import lombok.Value;

@Value
public class UploadingAsync {

    @Value
    public static class Command {
        String area;
        String key;
        AsyncDataProvider dataProvider;
    }

    AsyncStorage storage;

    public CompletableFuture<Result> upload(Command command) {

        final Object object = Object.create("1", "3");
        final CompletableFuture<Result> save = storage.uploadAsync(object);
        return null;
    }
}
