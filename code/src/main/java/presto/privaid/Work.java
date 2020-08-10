package presto.privaid;

import android.os.Bundle;

public class Work implements Runnable {
  static void checkIfInWorkThread() {
    if (!(Thread.currentThread() instanceof WorkThread)) {
      throw new IllegalStateException("Call expected from WorkThread");
    }
  }

  public enum Type {
    DISPATCH,
    RANDOMIZATION,
    DELIVERY,
    COMPENSATE
  }

  private final BaseDispatcher dispatcher;
  private String name = null;
  private Bundle bundle = null;
  private final Type type;

    public Work(BaseDispatcher dispatcher, Type type) {
      this.dispatcher = dispatcher;
      this.type = type;
  }

    public Work(BaseDispatcher dispatcher, Type type, String name, Bundle bundle) {
    this.dispatcher = dispatcher;
    this.type = type;
    this.name = name;
    this.bundle = bundle;
  }

  @Override
  public void run() {
    checkIfInWorkThread();
    if (type == Type.DISPATCH) {
      dispatcher.storeEvent(name, bundle);
    } else if (type == Type.RANDOMIZATION) {
      dispatcher.randomize();
    } else if (type == Type.DELIVERY) {
      dispatcher.send();
    } else if (type == Type.COMPENSATE) {
      dispatcher.compensate(name, bundle);
    }
  }

  static class WorkThread extends Thread {
    WorkThread(Runnable runnable, String s) {
      super(runnable, s);
    }
  }
}
