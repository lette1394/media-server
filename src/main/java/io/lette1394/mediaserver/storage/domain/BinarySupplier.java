package io.lette1394.mediaserver.storage.domain;

import java.io.InputStream;
import java.nio.ByteBuffer;
import org.reactivestreams.Publisher;

/**
 * A binary supplier which hide whether it support sync or async way from caller.<br/> <br/>
 *
 * <h2>Abstraction Level</h2>
 * Domain objects can use this {@link BinarySupplier} interface for hiding their specific binary
 * transfer logic whether it is sync or async way. Domain object can hold their code flow without
 * any considerations of implementation details.<br/> <br/>
 *
 * <h2>Restrictions</h2>
 * <ul>
 *   <li>The caller MUST check which way supported before to call {@link BinarySupplier#getSync()} or {@link BinarySupplier#getAsync()}
 *  </li>
 *
 * @author Jae eun Na
 * @see ListenableBinarySupplier
 * @see ControllableBinarySupplier
 * @see DelegatingBinarySupplier
 */
public interface BinarySupplier {
  /**
   * A caller must check using this method before to call {@link BinarySupplier#getSync()}
   *
   * @return whether this binary supported in sync way
   */
  boolean isSyncSupported();

  /**
   * A caller must check using this method before to call {@link BinarySupplier#getAsync()}
   *
   * @return whether this binary supported in async way
   */
  boolean isAsyncSupported();

  /**
   * Represents total binary length.
   *
   * @return binary length
   */
  long getLength();


  /**
   * @return InputStream: sync way binary
   *
   * @throws UnsupportedOperationException
   *   if {@link BinarySupplier#getSync()} return {@code false}
   */
  InputStream getSync() throws UnsupportedOperationException;

  /**
   * @return Publisher: async way binary
   *
   * @throws UnsupportedOperationException
   *   if {@link BinarySupplier#getAsync()} return {@code false}
   */
  Publisher<ByteBuffer> getAsync() throws UnsupportedOperationException;
}
