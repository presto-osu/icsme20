package presto.privaid;

import android.os.Bundle;

public class EventInfo {
  public long id;
  public String name;
  public Bundle params;

  public EventInfo(String name, Bundle params) {
    this.id = -1L;
    this.name = name;
    this.params = params;
  }

  public EventInfo(long id, String name, Bundle params) {
    this.id = id;
    this.name = name;
    this.params = params;
  }

  @Override
  public String toString() {
    return "id=" + id + ", name=" + name + ", params=" + params;
  }
}
