package presto.privaid.firebase;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import com.google.firebase.analytics.FirebaseAnalytics;
import presto.privaid.BaseDispatcher;
import presto.privaid.IRandomize;

import static presto.privaid.Utils.TAG;

public class Dispatcher extends BaseDispatcher {
  private final long INTERVAL_BETWEEN_SENDS = 2000; // 2 seconds
  private final FirebaseAnalytics firebaseAnalytics;

  public Dispatcher(Context context, IRandomize randomizer) {
    super(context, randomizer);
    firebaseAnalytics = FirebaseAnalytics.getInstance(appContext);
  }

  public Dispatcher(Context context) {
    this(context, IRandomize.NO_RANDOMIZATION);
  }

  @Override
  protected boolean send(String name, Bundle params) {
    Log.v(TAG, "sending event: " + name + " " + params);
    try {
      Thread.sleep(INTERVAL_BETWEEN_SENDS); // delay between two sends
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    firebaseAnalytics.logEvent(name, params);
    return true;
  }
}
