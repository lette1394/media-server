package io.lette1394.mediaserver.storage.infrastructure.springweb;


import io.lette1394.mediaserver.storage.domain.Storage;
import io.lette1394.mediaserver.storage.domain.Storage.StorageBuilder;
import io.lette1394.mediaserver.storage.infrastructure.filesystem.FileSystemBinaryRepository;
import io.lette1394.mediaserver.storage.usecase.Downloading;
import io.lette1394.mediaserver.storage.usecase.Uploading;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories
public class StorageConfiguration {
  private final static Storage springWebStorage;

  static {
    springWebStorage = StorageBuilder.builder()
      .objects(new FileSystemBinaryRepository("objects"))
      .binaries(new FileSystemBinaryRepository("binaries"))
      .build()
      .toStorage();
  }

  @Bean
  Downloading downloading() {
    return new Downloading(springWebStorage);
  }

  @Bean
  Uploading uploading() {
    return new Uploading(springWebStorage);
  }
}
