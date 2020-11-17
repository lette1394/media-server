package io.lette1394.mediaserver.storage.infrastructure.spring;

import io.lette1394.mediaserver.storage.domain.Object;
import javax.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;

@RequiredArgsConstructor
public class CollectingEventsTranslator implements Translator {
  private final Translator translator;

  @Override
  public <T> ResponseEntity<T> translate(Object<?> object, @Nullable Throwable throwable) {
    final ResponseEntity<java.lang.Object> translate = translator.translate(object, throwable);

    final ResponseEntity<java.lang.Object> objectResponseEntity = new ResponseEntity<>(
      translate.getBody(), translate.getHeaders(), translate.getStatusCode());



    return null;
  }
}
