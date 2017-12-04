package com.nelson.prioritythreadtest;

/**
 * Created by Nelson on 2017/12/4.
 */

public interface Task {

    void setFinished(boolean var1);

    boolean isFinished();

    void setError(Throwable var1);

    Throwable getError();
}