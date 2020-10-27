package io.lette1394.mediaserver.storage.domain.binary;

import java.nio.ByteBuffer;
import org.reactivestreams.Publisher;

/**
 * A binary supplier which hide whether it support sync or async way from caller.<br>
 * <br>
 *
 * <h2>Abstraction Level</h2>
 *
 * Domain objects can use this {@link BinarySupplier} interface for hiding their specific binary
 * transfer logic whether it is sync or async way. Domain object can hold their code flow without
 * any considerations of implementation details.<br>
 * <br>
 *
 * <h2>Restrictions</h2>
 *
 * @author Jae eun Na
 * @see ListenableBinarySupplier
 * @see ControllableBinarySupplier
 * @see BaseBinarySupplier
 */
public interface BinarySupplier {
  Publisher<ByteBuffer> getAsync();
}
