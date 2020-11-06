package io.lette1394.mediaserver.storage.infrastructure.spring;

import io.lette1394.mediaserver.storage.domain.Object;
import javax.annotation.Nullable;
import org.springframework.http.ResponseEntity;

@FunctionalInterface
public interface Translator {
  <T> ResponseEntity<T> translate(Object<?> object, @Nullable Throwable throwable);
}
