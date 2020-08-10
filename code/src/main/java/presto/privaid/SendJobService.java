package presto.privaid;

import android.app.job.JobParameters;
import android.app.job.JobService;

/** Sending events. */
public class SendJobService extends JobService implements IStopService {
  public static final String SEND_ACTION = "privaid.SEND";
  SendImpl<SendJobService> impl;

  private SendImpl<SendJobService> impl() {
    if (this.impl == null) {
      this.impl = new SendImpl<>(this);
    }
    return this.impl;
  }

  @Override
  public final void onCreate() {
    impl().onCreate();
    super.onCreate();
  }

  @Override
  public final void onDestroy() {
    impl().onDestroy();
    super.onDestroy();
  }

  @Override
  public boolean onStartJob(JobParameters params) {
    return impl().onStartJob(params);
  }

  @Override
  public boolean onStopJob(JobParameters params) {
    return false;
  }

  @Override
  public void callJobServiceFinished(JobParameters jobParameters, boolean needsReschedule) {
    jobFinished(jobParameters, false);
  }
}
