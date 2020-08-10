package presto.privaid;

import android.content.Context;
import android.os.Bundle;
import presto.privaid.firebase.Dispatcher;
import presto.privaid.firebase.TestRandomizer;

public class Tracker {
  private final BaseDispatcher dispatcher;
  private Scheduler scheduler;
  private static Tracker instance;

  private Tracker(Context context, BaseDispatcher dispatcher) {
    Context appContext = context.getApplicationContext();
    this.scheduler = new Scheduler(appContext);
    this.dispatcher = dispatcher;
    this.scheduler.schedule();
  }

  public static Tracker getInstance(Context context, BaseDispatcher dispatcher) {
    if (instance == null) {
      synchronized (Tracker.class) {
        if (instance == null) {
          instance = new Tracker(context.getApplicationContext(), dispatcher);
        }
      }
    }
    return instance;
  }

  public static Tracker getInstance(Context context) {
    return getInstance(context, new Dispatcher(context, IRandomize.NO_RANDOMIZATION));
  }

  public static Tracker getInstance() {
    if (instance == null) {
      synchronized (Tracker.class) {
        if (instance == null) {
          throw new RuntimeException("Tracker not initialized.");
        }
      }
    }
    return instance;
  }

  public BaseDispatcher getDispatcher() {
    return dispatcher;
  }

  public void logEvent(String name, Bundle params) {
    dispatcher.dispatch(name, params);
    scheduler.schedule();
  }
}
