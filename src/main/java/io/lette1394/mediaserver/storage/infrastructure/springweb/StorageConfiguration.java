package io.lette1394.mediaserver.storage.infrastructure.springweb;


import io.lette1394.mediaserver.storage.domain.Storage;
import io.lette1394.mediaserver.storage.domain.Storage.StorageBuilder;
import io.lette1394.mediaserver.storage.domain.binary.LengthAwareBinarySupplier;
import io.lette1394.mediaserver.storage.infrastructure.filesystem.FileSystemBinaryRepository;
import io.lette1394.mediaserver.storage.usecase.DownloadingBinary;
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
  private final static Storage<LengthAwareBinarySupplier> springWebStorage;

  static {
    springWebStorage = StorageBuilder.<LengthAwareBinarySupplier>builder()
      .objects(new FileSystemBinaryRepository("objects"))
      .binaries(new FileSystemBinaryRepository("binaries"))
      .build()
      .toStorage();
  }

  @Bean
  DownloadingBinary downloading() {
    return new DownloadingBinary(springWebStorage);
  }

  @Bean
  Uploading uploading() {
    return new Uploading(springWebStorage);
  }
}
