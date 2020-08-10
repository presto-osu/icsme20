package presto.privaid;

import android.os.Bundle;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Utils {
  public static final String TAG = "PRIVAID";

  public static Bundle stringMapToBundle(Map<String, String> map) {
    Bundle bundle = new Bundle();
    for (Map.Entry<String, String> entry : map.entrySet())
      bundle.putString(entry.getKey(), entry.getValue());
    return bundle;
  }

  public static Map<String, String> bundleToStringMap(Bundle bundle) {
    Map<String, String> map = new HashMap<>(bundle.size());
    for (String key : bundle.keySet()) map.put(key, bundle.getString(key, ""));
    return map;
  }

  public static String bundleToJsonString(Bundle bundle) {
    JSONObject jsonObject = new JSONObject();
    try {
      for (String key : bundle.keySet()) {
        Object value = bundle.get(key);
        jsonObject.put(key, value);
      }
    } catch (JSONException e) {
      Log.e(TAG, "Failed to convert event to JSON: " + e);
    }
    return jsonObject.toString();
  }

  public static Bundle jsonStringToBundle(String s) {
    Bundle bundle = new Bundle();
    try {
      JSONObject jsonObject = new JSONObject(s);
      for (Iterator<String> it = jsonObject.keys(); it.hasNext(); ) {
        String k = it.next();
        Object v = jsonObject.get(k);
        if (v instanceof String) bundle.putString(k, (String) v);
        else if (v instanceof Boolean) bundle.putBoolean(k, (Boolean) v);
        else if (v instanceof Integer) bundle.putInt(k, (Integer) v);
        else if (v instanceof Long) bundle.putLong(k, (Long) v);
        else if (v instanceof Double) bundle.putDouble(k, (Double) v);
      }
    } catch (JSONException e) {
      Log.e(TAG, "Failed to read event from JSON: " + e);
    }
    return bundle;
  }
}
