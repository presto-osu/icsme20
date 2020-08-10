package presto.privaid;

import android.util.Log;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static presto.privaid.Utils.TAG;

public class WorkThreadPoolExecutor extends ThreadPoolExecutor {
  private static final AtomicInteger threadId = new AtomicInteger();

  public WorkThreadPoolExecutor() {
    super(1, 1, 2, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>());
    this.setThreadFactory(
        new ThreadFactory() {
          @Override
          public final Thread newThread(Runnable runnable) {
            int n = threadId.incrementAndGet();
            return new Work.WorkThread(runnable, "presto-privaid-" + n);
          }
        });
    this.allowCoreThreadTimeOut(true);
  }

  @Override
  protected final <T> RunnableFuture<T> newTaskFor(Runnable runnable, T t) {
    return new FutureTask<T>(runnable, t) {
      @Override
      protected final void setException(Throwable throwable) {
        Log.w(TAG, "WorkThreadPoolExecutor: job failed with " + throwable);
        super.setException(throwable);
      }
    };
  }
}
