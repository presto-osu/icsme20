/*
 * Timer.java - part of the GATOR project
 *
 * Copyright (c) 2018 The Ohio State University
 *
 * This file is distributed under the terms described in LICENSE
 * in the root directory.
 */

package presto.privaid;

public class Timer {
  private long mStartTIme;

  public void start() {
    mStartTIme = System.currentTimeMillis();
  }

  public void clear() {
    mStartTIme = 0L;
  }

  public boolean checkTimeLimit(long l) {
    return mStartTIme == 0L || System.currentTimeMillis() - mStartTIme > l;
  }
}
