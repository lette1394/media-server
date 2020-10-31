package io.lette1394.mediaserver.storage.domain;

import org.reactivestreams.Publisher;

/**
 * A binary supplier which hide whether it support sync or async way from caller.<br>
 * <br>
 *
 * <h2>Abstraction Level</h2>
 * <p>
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


// TODO: 이거 package-private 으로 해야만 한다.
//  core domain 외부에서 직접 생성하는 경우를 방지해야 하는데... 그걸 강제하는게 가능한가?
//  안될거같다 왜냐면... usecase 에서 써야해서...ㅠ
@FunctionalInterface
public interface BinarySupplier<BUFFER extends Payload> {

  Publisher<BUFFER> getAsync();
}
