package io.lette1394.mediaserver.storage.infrastructure.springwebflux;


import io.lette1394.mediaserver.storage.infrastructure.DataBufferPayload;
import io.lette1394.mediaserver.storage.infrastructure.filesystem.DataBufferFileSystemRepository;
import io.lette1394.mediaserver.storage.infrastructure.spring.YamlPropertySourceFactory;
import io.lette1394.mediaserver.storage.usecase.Uploading;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@SpringBootConfiguration
@ComponentScan("io.lette1394.mediaserver.storage.infrastructure.springwebflux")
@EnableAutoConfiguration(exclude = {
  DataSourceAutoConfiguration.class,
})
@PropertySource(value = "classpath:springwebflux-application.properties")
public class SpringWebFluxConfiguration {
//  private final static Storage springWebStorage;
//
//  static {
//    springWebStorage = StorageBuilder.<LengthAwareBinarySupplier>builder()
//      .objects(new FileSystemBinaryRepository("objects"))
//      .binaries(new FileSystemBinaryRepository("binaries"))
//      .build()
//      .toStorage();
//  }

  @Bean
  Uploading<DataBufferPayload> uploading() {
//    return new Uploading(springWebStorage);

    return new Uploading<>(
      new DataBufferFileSystemRepository("out/binaries"),
      new DataBufferFileSystemRepository("out/objects")
    );
  }
}
