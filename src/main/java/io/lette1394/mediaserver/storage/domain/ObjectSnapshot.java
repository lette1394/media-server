package io.lette1394.mediaserver.storage.domain;

import static io.lette1394.mediaserver.storage.domain.ObjectType.FULFILLED;
import static io.lette1394.mediaserver.storage.domain.ObjectType.INITIAL;
import static io.lette1394.mediaserver.storage.domain.ObjectType.PENDING;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

// TODO : test case
@AllArgsConstructor
public class ObjectSnapshot {

  private State state;

  public static ObjectSnapshot initial() {
    return new ObjectSnapshot(
      InitialState.builder()
        .size(0L)
        .progressingSize(0L)
        .command(Command.NO_OPERATION)
        .objectType(INITIAL)
        .build());
  }

  public static ObjectSnapshot byObjectType(ObjectType objectType, long size) {
    if (objectType == PENDING) {
      return new ObjectSnapshot(PendingState.builder()
        .size(size)
        .progressingSize(0L)
        .objectType(PENDING)
        .command(Command.NO_OPERATION)
        .build());
    }
    if (objectType == FULFILLED) {
      return new ObjectSnapshot(FulfilledState.builder()
        .size(size)
        .progressingSize(0L)
        .objectType(FULFILLED)
        .command(Command.NO_OPERATION)
        .build());
    }
    throw new IllegalStateException("no other states");
  }

  ObjectSnapshot update(long progressingSize) {
    this.state.update(progressingSize);
    return this;
  }

  ObjectSnapshot update(ObjectType objectType) {
    this.state = state.shiftTo(objectType);
    return this;
  }

  ObjectSnapshot update(Command command) {
    this.state.update(command);
    return this;
  }

  public long getProgressingSize() {
    return state.getProgressingSize();
  }

  public long getSize() {
    return state.getSize();
  }

  public ObjectType getObjectType() {
    return state.getObjectType();
  }

  public Command getCommand() {
    return state.getCommand();
  }

  public boolean is(ObjectType objectType) {
    return state.is(objectType);
  }

  public boolean is(Command command) {
    return state.is(command);
  }

  @Getter
  @AllArgsConstructor
  private static abstract class State {
    protected long size;
    protected long progressingSize;
    protected ObjectType objectType;
    protected Command command;

    abstract long getProgressingSize();

    abstract long getSize();

    abstract State shiftTo(ObjectType objectType);

    private boolean is(ObjectType objectType) {
      return this.objectType.is(objectType);
    }

    private boolean is(Command command) {
      return this.command.is(command);
    }

    private void update(long progressingSize) {
      this.progressingSize = progressingSize;
    }

    private void update(Command command) {
      this.command = command;
    }
  }

  private static class InitialState extends State {
    @Builder
    public InitialState(long size, long progressingSize,
      ObjectType objectType, Command command) {
      super(size, progressingSize, objectType, command);
    }

    @Override
    long getSize() {
      return 0L;
    }

    @Override
    long getProgressingSize() {
      return progressingSize;
    }

    @Override
    State shiftTo(ObjectType objectType) {
      if (objectType == PENDING) {
        return PendingState.builder()
          .size(progressingSize)
          .progressingSize(0L)
          .objectType(PENDING)
          .command(command)
          .build();
      }
      if (objectType == FULFILLED) {
        return FulfilledState.builder()
          .size(progressingSize)
          .progressingSize(0L)
          .objectType(FULFILLED)
          .command(command)
          .build();
      }
      throw new IllegalStateException("not allowed: initial -> initial");
    }
  }

  private static class PendingState extends State {
    @Builder
    public PendingState(long size, long progressingSize,
      ObjectType objectType, Command command) {
      super(size, progressingSize, objectType, command);
    }

    @Override
    long getSize() {
      return size;
    }

    @Override
    long getProgressingSize() {
      return progressingSize;
    }

    @Override
    State shiftTo(ObjectType objectType) {
      if (objectType == PENDING) {
        return PendingState.builder()
          .size(size + progressingSize)
          .progressingSize(0L)
          .objectType(PENDING)
          .command(command)
          .build();
      }
      if (objectType == FULFILLED) {
        return FulfilledState.builder()
          .size(size + progressingSize)
          .progressingSize(0L)
          .objectType(FULFILLED)
          .command(command)
          .build();
      }
      return this;
    }
  }

  private static class FulfilledState extends State {
    @Builder
    public FulfilledState(long size, long progressingSize,
      ObjectType objectType, Command command) {
      super(size, progressingSize, objectType, command);
    }

    @Override
    long getSize() {
      return size;
    }

    @Override
    long getProgressingSize() {
      return progressingSize;
    }

    @Override
    State shiftTo(ObjectType objectType) {
      if (objectType == PENDING) {
        return PendingState.builder()
          .size(progressingSize)
          .progressingSize(0L)
          .objectType(PENDING)
          .command(command)
          .build();
      }
      if (objectType == FULFILLED) {
        return FulfilledState.builder()
          .size(progressingSize)
          .progressingSize(0L)
          .objectType(FULFILLED)
          .command(command)
          .build();
      }
      return this;
    }
  }
}
