package presto.privaid;

import android.app.job.JobParameters;

public class StopServiceJob implements Runnable {
  private final JobParameters jobParameters;
  private final IStopService service;

  public StopServiceJob(IStopService service, JobParameters jobParameters) {
    this.jobParameters = jobParameters;
    this.service = service;
  }

  @Override
  public void run() {
    service.callJobServiceFinished(jobParameters, false);
  }
}
