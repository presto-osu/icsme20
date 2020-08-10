package presto.privaid;

import android.app.job.JobParameters;

public interface IStopService {
  void callJobServiceFinished(JobParameters jobParameters, boolean needsReschedule);
}
