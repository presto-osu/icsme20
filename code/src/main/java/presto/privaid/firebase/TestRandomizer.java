package presto.privaid.firebase;

import android.os.Bundle;
import android.util.Log;
import presto.privaid.EventInfo;
import presto.privaid.IRandomize;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static presto.privaid.Utils.TAG;

public class TestRandomizer implements IRandomize {
  double epsilon;
  double pSend;

  public TestRandomizer(double epsilon) {
    this.epsilon = epsilon;
    pSend = Math.exp(epsilon / 2) / (1 + Math.exp(epsilon / 2));
    Log.d(TAG, String.format("Epsilon: %.2f, pSend = %.3f", epsilon, pSend));
  }

  private boolean match(Bundle small, Bundle big) {
    for (String key : small.keySet()) {
      Object smallV = small.get(key);
      Object bigV = big.get(key);
      if (!smallV.equals(bigV)) return false;
    }
    return true;
  }

  @Override
  public Set<EventInfo> randomize(EventInfo event) {
    Set<EventInfo> ret = new HashSet<>();
    Random rand = new Random();

    Log.d(TAG, "Randomizing event: " + event);
    for (Bundle b : ContentDictionaryManager.getContentDictionary(event.name)) {
      if (match(b, event.params)) {
//        Log.d(TAG, "Event matched: " + b);
        if (rand.nextDouble() < pSend) {
          Bundle bundle = new Bundle(event.params);
          ret.add(new EventInfo(event.name, bundle));
        }
      } else if (rand.nextDouble() < 1 - pSend) {
//        Log.d(TAG, "Noise event : " + b);
        Bundle bundle = new Bundle(event.params);
        bundle.putAll(b);
        ret.add(new EventInfo(event.name, bundle));
      }
    }

    Log.d(TAG, "# Randomized events: " + ret.size());
    return ret;
  }

  @Override
  public double getPSend() {
    return pSend;
  }
}
