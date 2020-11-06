package io.lette1394.mediaserver.storage.infrastructure.spring.mvc;


import io.lette1394.mediaserver.storage.infrastructure.DataBufferPayload;
import io.lette1394.mediaserver.storage.infrastructure.filesystem.DataBufferFileSystemRepository;
import io.lette1394.mediaserver.storage.usecase.Uploading;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@SpringBootConfiguration
@ComponentScan("io.lette1394.mediaserver.storage.infrastructure.spring.mvc")
@EnableAutoConfiguration(exclude = {
  DataSourceAutoConfiguration.class,
})
@PropertySource(value = "classpath:springwebmvc-application.properties")
public class SpringWebMvcConfiguration {
//  @Bean
//  Uploading<ByteBufferPayload> uploading() {
//    return new Uploading<>(
//      new ByteBufferFileSystemRepository("out/binaries"),
//      new ByteBufferFileSystemRepository("out/objects")
//    );
//  }
//

  @Bean
  Uploading<DataBufferPayload> uploading() {
    return new Uploading<>(
      new DataBufferFileSystemRepository("out/binaries"),
      new DataBufferFileSystemRepository("out/objects")
    );
  }


  @Bean
  TomcatServletWebServerFactory webserverNetty() {
    return new TomcatServletWebServerFactory();
  }
}
