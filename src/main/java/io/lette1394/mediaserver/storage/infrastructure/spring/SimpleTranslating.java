package io.lette1394.mediaserver.storage.infrastructure.spring;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;
import static io.vavr.Predicates.instanceOf;
import static io.vavr.Predicates.isNull;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.ResponseEntity.badRequest;
import static org.springframework.http.ResponseEntity.notFound;
import static org.springframework.http.ResponseEntity.status;

import io.lette1394.mediaserver.common.ContractViolationException;
import io.lette1394.mediaserver.common.PolicyViolationException;
import io.lette1394.mediaserver.storage.domain.Object;
import io.lette1394.mediaserver.storage.domain.ObjectNotFoundException;
import io.lette1394.mediaserver.storage.domain.OperationCanceledException;
import java.util.concurrent.CompletionException;
import javax.annotation.Nullable;
import org.springframework.http.ResponseEntity;

public class SimpleTranslating implements Translator {
  @Override
  public <T> ResponseEntity<T> translate(Object<?> object, @Nullable Throwable throwable) {
    if (throwable == null) {
      return translateResponse(object);
    }
    throwable.printStackTrace();
    return translateException(throwable);
  }

  private <T> ResponseEntity<T> translateResponse(Object<?> object) {
    return ResponseEntity.ok().build();
  }

  private <T> ResponseEntity<T> translateException(Throwable throwable) {
    return Match(throwable)
      .of(
        Case($(isNull()),
          () -> status(INTERNAL_SERVER_ERROR).build()),
        Case($(instanceOf(CompletionException.class)),
          () -> translateException(throwable.getCause())),
        Case($(instanceOf(OperationCanceledException.class)),
          () -> translateException(throwable.getCause())),
        Case($(instanceOf(PolicyViolationException.class)),
          (e) -> badRequest()
            .header("x-ms-error-code", e.getCode())
            .header("x-ms-error-message", e.getMessage())
            .build()),
        Case($(instanceOf(ObjectNotFoundException.class)),
          () -> notFound().build()),
        Case($(instanceOf(ObjectNotFoundException.class)),
          () -> notFound().build()),
        Case($(instanceOf(ContractViolationException.class)),
          () -> status(INTERNAL_SERVER_ERROR).build()),
        Case($(),
          () -> status(INTERNAL_SERVER_ERROR).build()));
  }
}
