package presto.privaid.firebase;

import android.os.Bundle;
import android.util.Log;
import presto.privaid.DatabaseController;
import presto.privaid.WorkThreadPoolExecutor;

import java.util.*;

import static presto.privaid.Utils.TAG;

public class ContentDictionaryManager {
  private static Map<String, Set<Bundle>> dictionaries = new HashMap<>();
  private final static WorkThreadPoolExecutor compensateExecutor = new WorkThreadPoolExecutor();

  public static Set<Bundle> getContentDictionary(String name) {
    if (!dictionaries.containsKey(name)) {
      dictionaries.put(name, new HashSet<>());
      DatabaseController.getInstance().createContentTable(name);
    }
    return dictionaries.get(name);
  }

  private static boolean bundleEquals(Bundle b1, Bundle b2) {
    if (b1.size() != b2.size()) {
      return false;
    }

    for (String key : b1.keySet()) {
      if (!b2.containsKey(key)) {
        return false;
      }

      if (!b1.get(key).equals(b2.get(key))) {
        return false;
      }
    }

    return true;
  }

  private static boolean contains(String name, Bundle bundle) {
    Set<Bundle> dict = getContentDictionary(name);
    for (Bundle b : dict) {
      if (bundleEquals(b, bundle)) {
        return true;
      }
    }

    return false;
  }

  public static void addContent(String name, Bundle bundle) {
    if (contains(name, bundle)) {
      Log.d(TAG, bundle + " already in " + name);
      return;
    }
    getContentDictionary(name).add(bundle);
    DatabaseController.getInstance().addContent(name, bundle);
//    Log.d(TAG, "Adding " + bundle + " to " + name +  ", Dictionary size: " + getContentDictionary(name).size());
    Log.d(TAG, "Dictionary table size (" + name + "): " + DatabaseController.getInstance().numberOfDictionaryElements(name));
//    compensateExecutor.submit(new Work(Tracker.getInstance().getDispatcher(), Work.Type.COMPENSATE, name, bundle));
  }

  public static void addAllContents(String name, String bundleKey, Collection<Object> values) {
    Iterator<Object> it = values.iterator();
    while (it.hasNext()) {
      Object obj = it.next();
      // dummy statement, insert instrumentation to after the following
      obj = obj;
      // cast obj to appropriate type (from spec) into a local variable
      // use the local variable for regular instrumentation
    }
  }
}
