package io.lette1394.mediaserver;

import static io.lette1394.mediaserver.suite.TestSuiteTag.CHECK_MEMORY_LEAK;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;

@Tag(CHECK_MEMORY_LEAK)
public abstract class MemoryLeakTest {
  protected MemoryLeakDetectableByteBufAllocator memoryLeakDetectableByteBufAllocator;

  @BeforeEach
  protected void beforeEach() {
    createAllocator();
  }

  void createAllocator() {
    memoryLeakDetectableByteBufAllocator = MemoryLeakDetectableByteBufAllocator.create();
  }

  @AfterEach
  protected void afterEach() {
    checkForMemoryLeaks();
  }

  void checkForMemoryLeaks() {
    memoryLeakDetectableByteBufAllocator.checkForLeaks();
  }
}
