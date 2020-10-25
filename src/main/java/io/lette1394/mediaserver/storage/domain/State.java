package io.lette1394.mediaserver.storage.domain;

public enum State {
  INITIAL {
    @Override
    public boolean isInitial() {
      return true;
    }
  },
  PENDING {
    @Override
    public boolean isPending() {
      return true;
    }
  },
  FULFILLED {
    @Override
    public boolean isFulfilled() {
      return true;
    }
  };

  public boolean isInitial() {
    return false;
  }

  public boolean isPending() {
    return false;
  }

  public boolean isFulfilled() {
    return false;
  }
}