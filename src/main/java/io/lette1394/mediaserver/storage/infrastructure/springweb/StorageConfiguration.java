package io.lette1394.mediaserver.storage.infrastructure.springweb;


import io.lette1394.mediaserver.storage.domain.LengthAwareBinarySupplier;
import io.lette1394.mediaserver.storage.infrastructure.filesystem.FileSystemBinaryRepository;
import io.lette1394.mediaserver.storage.usecase.Uploading;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootConfiguration
@ComponentScan("io.lette1394.mediaserver.storage.infrastructure.springweb")
@EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
public class StorageConfiguration {
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
  Uploading uploading() {
//    return new Uploading(springWebStorage);
    return null;
  }
}
