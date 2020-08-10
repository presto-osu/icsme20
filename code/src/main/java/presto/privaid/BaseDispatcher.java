package presto.privaid;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import java.util.*;

import static presto.privaid.Utils.TAG;

/**
 * Record, randomize, and send an event. The process of dispatching includes 3 tasks, named
 * 'dispatch', 'randomization', 'delivery', by {@link Work.Type}.
 *
 * <ul>
 *   <li>Dispatch task stores the event to local DB 'events'.
 *   <li>Randomization task perturbs each real event.
 *   <li>Delivery task delivers a randomized event to the underlying analytics library.
 * </ul>
 */
public abstract class BaseDispatcher {
  private final WorkThreadPoolExecutor deliverExecutor = new WorkThreadPoolExecutor();
  private final WorkThreadPoolExecutor dispatchExecutor = new WorkThreadPoolExecutor();
  private final WorkThreadPoolExecutor randomizeExecutor = new WorkThreadPoolExecutor();
  protected final Context appContext;
  private final DatabaseController databaseController;
  protected final IRandomize randomizer;
  private final long MAX_HITS_PER_DISPATCH = 20;

  public BaseDispatcher(Context context, IRandomize randomizer) {
    appContext = context;
    databaseController = DatabaseController.getInstance(context);
    this.randomizer = randomizer;
  }

  /** Dispatching a event. */
  void dispatch(String name, Bundle params) {
    dispatchExecutor.submit(new Work(this, Work.Type.DISPATCH, name, params));
  }

  /** Record a real event in `events' DB and submit randomization tasks. */
  void storeEvent(String name, Bundle params) {
    databaseController.beginTransaction();
    try {
      long id = databaseController.storeEvent(name, params);
      databaseController.setTransactionSuccessful();
    } finally {
      databaseController.endTransaction();
    }

    // submit randomization works
//    randomizeExecutor.submit(new Work(this, Work.Type.RANDOMIZATION));
  }

  /**
   * Read events from 'events' DB and perturb one by one. The real events are deleted in the DB
   * after randomization. Perturbed events are stored in `random_events' DB.
   */
  void randomize() {
    if (databaseController.numberOfStoredEvents() > 0L) {
      for (EventInfo event : databaseController.readEvents(MAX_HITS_PER_DISPATCH)) {
        Log.v(TAG, "Perturbing event " + event);
        Set<EventInfo> randEvents = randomizer.randomize(event);
        databaseController.beginTransaction();
        try {
          for (EventInfo e : randEvents) {
            databaseController.storeRandomizedEvent(e.name, e.params);
          }
          databaseController.storeProcessedEvent(event.name, event.params);
          databaseController.deleteEvent(event.id);
          databaseController.setTransactionSuccessful();
        } finally {
          databaseController.endTransaction();
        }
      }
    }
  }

  /** Deliver unsent events to the underlying analytics library. */
  void deliver() {
    deliverExecutor.submit(new Work(this, Work.Type.DELIVERY));
  }

  /** Read from 'random_events' DB and send a series of events. */
  void send() {
    List<EventInfo> randEvents = databaseController.readRandomizedEvents(MAX_HITS_PER_DISPATCH);
    for (EventInfo event : randEvents) {
      if (send(event.name, event.params)) {
        databaseController.beginTransaction();
        try {
//          databaseController.deleteRandomizedEvent(event.id);
          databaseController.setTransactionSuccessful();
        } finally {
          databaseController.endTransaction();
        }
      }
    }
  }

  /**
   * Send an event.
   *
   * @param name the name of the event
   * @param params the parameters of the event in Bundle; some libraries, e.g., Flurry, use maps;
   *     use {@link Utils#bundleToStringMap(Bundle)} and {@link Utils#stringMapToBundle(Map)} for
   *     transformation
   * @return true if success and as a result the event is deleted in DB; false if failed and the
   *     event remains in the DB
   */
  protected abstract boolean send(String name, Bundle params);

  /** Adds the events generated from adding new elements into the content dictionary into the table
   * of randomized events. */
  public void compensate(String name, Bundle params) {
    List<EventInfo> processedEvents = databaseController.readProcessedEvents(MAX_HITS_PER_DISPATCH);
    Random rand = new Random();
    int count = 0;
    databaseController.beginTransaction();
    try {
      for (EventInfo event : processedEvents) {
        if (event.name.equals(name) && rand.nextDouble() < 1 - randomizer.getPSend()) {
          Bundle bundle = new Bundle(event.params);
          bundle.putAll(params);
          databaseController.storeRandomizedEvent(name, bundle);
//          Log.d(TAG, "Compensating " + name + ": " + params);
          count++;
        }
      }
      databaseController.setTransactionSuccessful();
    } finally {
      databaseController.endTransaction();
    }
    if (count > 0) {
      Log.d(TAG, "New element " + name + " " + params + " added " + count + " times");
    }
  }
}
