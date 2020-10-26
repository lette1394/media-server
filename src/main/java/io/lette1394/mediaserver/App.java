package io.lette1394.mediaserver;

import io.lette1394.mediaserver.storage.infrastructure.springweb.StorageConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class App {
  public static void main(String[] args) {
    new SpringApplicationBuilder()
      .parent(StorageConfiguration.class).web(WebApplicationType.SERVLET)
      .run(args);
  }
}
