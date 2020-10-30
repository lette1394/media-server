package io.lette1394.mediaserver;

import io.lette1394.mediaserver.storage.infrastructure.springmvc.SpringWebMvcConfiguration;
import io.lette1394.mediaserver.storage.infrastructure.springwebflux.SpringWebFluxConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class App {
  public static void main(String[] args) {
    new SpringApplicationBuilder(SpringWebMvcConfiguration.class)
      .web(WebApplicationType.SERVLET)
      .run(args);

    new SpringApplicationBuilder(SpringWebFluxConfiguration.class)
      .web(WebApplicationType.REACTIVE)
      .run(args);
//
//    new SpringApplicationBuilder(App.class)
//      .parent(App.class).web(WebApplicationType.NONE)
//      .child(SpringWebFluxConfiguration.class).web(WebApplicationType.REACTIVE)
//      .sibling(SpringWebMvcConfiguration.class).web(WebApplicationType.SERVLET)
//      .run(args);
  }
}
