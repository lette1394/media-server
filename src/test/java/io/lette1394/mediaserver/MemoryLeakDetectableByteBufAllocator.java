package io.lette1394.mediaserver;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakDetector.Level;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MemoryLeakDetectableByteBufAllocator extends PooledByteBufAllocator {
  private final List<ByteBuf> trackedBuffers = new ArrayList<>();

  private MemoryLeakDetectableByteBufAllocator() {
    super(false);
  }

  public static MemoryLeakDetectableByteBufAllocator create() {
    final ResourceLeakDetector.Level level = ResourceLeakDetector.getLevel();
    if (level != Level.PARANOID) {
      throw new AssertionError(
        "Required netty leak detection level: paranoid. You can add following jvm option: [-Dio.netty.leakDetection.level=PARANOID]");
    }
    return new MemoryLeakDetectableByteBufAllocator();
  }

  @Override
  protected ByteBuf newHeapBuffer(int initialCapacity, int maxCapacity) {
    return track(super.newHeapBuffer(initialCapacity, maxCapacity));
  }

  @Override
  protected ByteBuf newDirectBuffer(int initialCapacity, int maxCapacity) {
    return track(super.newDirectBuffer(initialCapacity, maxCapacity));
  }

  @Override
  public CompositeByteBuf compositeHeapBuffer(int maxNumComponents) {
    return track(super.compositeHeapBuffer(maxNumComponents));
  }

  @Override
  public CompositeByteBuf compositeDirectBuffer(int maxNumComponents) {
    return track(super.compositeDirectBuffer(maxNumComponents));
  }

  private synchronized CompositeByteBuf track(CompositeByteBuf byteBuf) {
    trackedBuffers.add(Objects.requireNonNull(byteBuf));
    return byteBuf;
  }

  private synchronized ByteBuf track(ByteBuf byteBuf) {
    trackedBuffers.add(Objects.requireNonNull(byteBuf));
    return byteBuf;
  }

  public void checkForLeaks() {
    long referencedBuffersCount = 0;
    synchronized (this) {
      referencedBuffersCount = trackedBuffers
        .stream()
        .filter(byteBuf -> byteBuf.refCnt() > 0)
        .count();
      // Make tracked buffers eligible for GC
      trackedBuffers.clear();
    }
    if (referencedBuffersCount > 0) {
      // Trigger a GC. This will hopefully (but not necessarily) print
      // details about detected leaks to standard error before the error
      // is thrown.
      System.gc();
      throw new AssertionError("Found a netty ByteBuf leak!");
    }
  }
}