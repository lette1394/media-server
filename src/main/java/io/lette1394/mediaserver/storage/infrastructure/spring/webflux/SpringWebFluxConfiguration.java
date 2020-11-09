package io.lette1394.mediaserver.storage.infrastructure.spring.webflux;


import io.lette1394.mediaserver.storage.domain.ObjectFactory;
import io.lette1394.mediaserver.storage.infrastructure.DataBufferPayload;
import io.lette1394.mediaserver.storage.infrastructure.filesystem.DataBufferFileSystemRepository;
import io.lette1394.mediaserver.storage.infrastructure.spring.SimpleTranslating;
import io.lette1394.mediaserver.storage.infrastructure.spring.Translator;
import io.lette1394.mediaserver.storage.usecase.Downloading;
import io.lette1394.mediaserver.storage.usecase.Uploading;
import io.lette1394.mediaserver.storage.usecase.copy.CopyStrategy;
import io.lette1394.mediaserver.storage.usecase.copy.Copying;
import io.lette1394.mediaserver.storage.usecase.copy.HardCopying;
import io.lette1394.mediaserver.storage.usecase.copy.ReplicatingHardCopying;
import io.lette1394.mediaserver.storage.usecase.copy.ReplicatingHardCopying.ReplicaFollowingObjectRepository;
import io.lette1394.mediaserver.storage.usecase.copy.SoftCopying;
import io.lette1394.mediaserver.storage.usecase.copy.SoftCopying.SoftCopyFollowingObjectRepository;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.ChannelOption;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.reactive.config.EnableWebFlux;

@Configuration
@EnableWebFlux
@SpringBootConfiguration
@ComponentScan("io.lette1394.mediaserver.storage.infrastructure.spring.webflux")
@EnableAutoConfiguration(exclude = {
  DataSourceAutoConfiguration.class,
})
@PropertySource(value = "classpath:springwebflux-application.properties")
public class SpringWebFluxConfiguration {
  private static final DataBufferFileSystemRepository filesystem
    = new DataBufferFileSystemRepository("out/storage");

  @Bean
  Uploading<DataBufferPayload> uploading() {
    return new Uploading<>(filesystem, filesystem);
  }

  @Bean
  Downloading<DataBufferPayload> downloading() {
    return new Downloading<>(filesystem,
      new SoftCopyFollowingObjectRepository<>(
        new ReplicaFollowingObjectRepository<>(filesystem)));
  }

  @Bean
  Copying<DataBufferPayload> copying() {
    final ObjectFactory<DataBufferPayload> objectFactory = new ObjectFactory<>();

    final CopyStrategy<DataBufferPayload> hardCopying = new HardCopying<>(objectFactory, filesystem, filesystem);
    final CopyStrategy<DataBufferPayload> softCopying = new SoftCopying<>(objectFactory, filesystem);
    final CopyStrategy<DataBufferPayload> replicatingHardCopying = new ReplicatingHardCopying<>(hardCopying, filesystem);

    return Copying.<DataBufferPayload>builder()
      .hardCopying(hardCopying)
      .softCopying(softCopying)
      .replicatingHardCopying(replicatingHardCopying)
      .objectRepository(filesystem)
      .build();
  }

  @Bean
  @Primary
  Translator translator() {
    return new SimpleTranslating();
  }

  @Bean
  @Profile({"local", "alpha", "beta", "rc"})
  Translator debuggingTranslator() {
    return null;
  }

  @Bean
  NettyReactiveWebServerFactory webServerNetty() {
    final NettyReactiveWebServerFactory factory = new NettyReactiveWebServerFactory();
    factory.addServerCustomizers(httpServer -> httpServer.tcpConfiguration(tcpServer -> {
      return tcpServer
        .option(ChannelOption.ALLOCATOR, new PooledByteBufAllocator(true))
        .option(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator());
    }));
    return factory;
  }
}
