package io.lette1394.mediaserver.storage.infrastructure.springwebflux;


import io.lette1394.mediaserver.storage.infrastructure.DataBufferPayload;
import io.lette1394.mediaserver.storage.infrastructure.filesystem.DataBufferFileSystemRepository;
import io.lette1394.mediaserver.storage.usecase.Uploading;
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
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.reactive.config.EnableWebFlux;

@Configuration
@EnableWebFlux
@SpringBootConfiguration
@ComponentScan("io.lette1394.mediaserver.storage.infrastructure.springwebflux")
@EnableAutoConfiguration(exclude = {
  DataSourceAutoConfiguration.class,
})
@PropertySource(value = "classpath:springwebflux-application.properties")
public class SpringWebFluxConfiguration {

  @Bean
  Uploading<DataBufferPayload> uploading() {
    return new Uploading<>(
      new DataBufferFileSystemRepository("out/binaries"),
      new DataBufferFileSystemRepository("out/objects")
    );
  }

  @Bean
  NettyReactiveWebServerFactory webserverNetty() {
    final NettyReactiveWebServerFactory factory = new NettyReactiveWebServerFactory();
    factory.addServerCustomizers(httpServer -> httpServer.tcpConfiguration(tcpServer -> {
      return tcpServer
        .option(ChannelOption.ALLOCATOR, new PooledByteBufAllocator(true))
        .option(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator());
    }));
    return factory;
  }
}
