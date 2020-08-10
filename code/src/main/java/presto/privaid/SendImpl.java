package presto.privaid;

import android.app.job.JobParameters;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import static presto.privaid.Utils.TAG;

public class SendImpl<T extends Context> {
  private final Handler mHandler;
  private final T service;

  SendImpl(T t) {
    service = t;
    mHandler = new Handler();
  }

  public void onCreate() {
    Log.v(TAG, service.getClass().getSimpleName() + " is starting up");
  }

  public void onDestroy() {
    Log.v(TAG, service.getClass().getSimpleName() + " is shutting down");
  }

  private void dispatch(final JobParameters jobParameters) {
    try {
      Tracker.getInstance().getDispatcher().deliver();
    } catch (Exception ignored) {
    } finally {
      mHandler.post(new StopServiceJob((IStopService) service, jobParameters));
    }
  }

  public boolean onStartJob(JobParameters jobParameters) {
    String string = jobParameters.getExtras().getString("action");
    Log.v(TAG, "SendJobService called. Action: " + string);
    if (SendJobService.SEND_ACTION.equals(string)) {
      dispatch(jobParameters);
    }
    return true;
  }
}
