package presto.privaid;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.PersistableBundle;
import android.util.Log;

import static presto.privaid.Utils.TAG;

/**
 * Schedule a sending job of events. Note that this implementation is not periodic. Each job is
 * scheduled when {@link Tracker} is initialized and when there is a new event.
 */
public class Scheduler {
  private final Context appContext;
  private boolean hasScheduled;
  private Integer jobId;
    private long SCHEDULE_ALARM_MILLIS = 60000; // 1 minute delay for actual sending

  Scheduler(Context context) {
    appContext = context;
  }

    void schedule() {
      cancel();
      hasScheduled = true;
      JobScheduler jobScheduler =
          (JobScheduler) appContext.getSystemService(Context.JOB_SCHEDULER_SERVICE);
      ComponentName componentName = new ComponentName(appContext, SendJobService.class);
      JobInfo.Builder builder = new JobInfo.Builder(getJobId(), componentName);
      builder.setMinimumLatency(SCHEDULE_ALARM_MILLIS);
      builder.setOverrideDeadline(SCHEDULE_ALARM_MILLIS << 1);
      //    builder.setPeriodic(SCHEDULE_ALARM_MILLIS);
      PersistableBundle persistableBundle = new PersistableBundle();
      persistableBundle.putString("action", SendJobService.SEND_ACTION);
      builder.setExtras(persistableBundle);
      JobInfo jobInfo = builder.build();
      try {
        jobScheduler.schedule(jobInfo);
        Log.v(TAG, "Scheduling upload with JobScheduler. JobID: " + getJobId());
      } catch (IllegalArgumentException ex) {
        Log.w(TAG, "SendJobService not available: " + ex.getMessage());
      }
  }

  private void cancel() {
    hasScheduled = false;
    JobScheduler jobScheduler =
        (JobScheduler) appContext.getSystemService(Context.JOB_SCHEDULER_SERVICE);
    Log.v(TAG, "Cancelling job. JobID: " + getJobId());
    jobScheduler.cancel(getJobId());
  }

  private int getJobId() {
    if (jobId == null) {
      String string = String.valueOf(appContext.getPackageName());
      jobId =
          (string.length() != 0 ? "presto/privaid".concat(string) : "presto/privaid").hashCode();
    }
    return jobId;
  }
}
