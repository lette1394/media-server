package io.lette1394.mediaserver.storage.infrastructure.springweb;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StorageController {

  @GetMapping("/{area}/{key}")
  ResponseEntity<?> getObject(@PathVariable String area, @PathVariable String key) {

    return ResponseEntity.ok(null);
  }
}
