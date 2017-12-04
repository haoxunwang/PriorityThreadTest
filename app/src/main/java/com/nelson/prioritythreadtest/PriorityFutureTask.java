package com.nelson.prioritythreadtest;

import android.renderscript.RenderScript.Priority;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * Created by Nelson on 2017/12/4.
 */

public class PriorityFutureTask<V> extends FutureTask<V> implements Dependency<Task>,
        PriorityProvider, Task, DelegateProvider {

    final Object delegate;

    public PriorityFutureTask(Callable<V> callable) {
        super(callable);
        this.delegate = this.checkAndInitDelegate(callable);
    }

    public PriorityFutureTask(Runnable runnable, V result) {
        super(runnable, result);
        this.delegate = this.checkAndInitDelegate(runnable);
    }

    public int compareTo(Object another) {
        return ((PriorityProvider) this.getDelegate()).compareTo(another);
    }

    public void addDependency(Task task) {
        ((Dependency) ((PriorityProvider) this.getDelegate())).addDependency(task);
    }

    public Collection<Task> getDependencies() {
        return ((Dependency) ((PriorityProvider) this.getDelegate())).getDependencies();
    }

    public boolean areDependenciesMet() {
        return ((Dependency) ((PriorityProvider) this.getDelegate())).areDependenciesMet();
    }

    public Priority getPriority() {
        return ((PriorityProvider) this.getDelegate()).getPriority();
    }

    public void setFinished(boolean finished) {
        ((Task) ((PriorityProvider) this.getDelegate())).setFinished(finished);
    }

    public boolean isFinished() {
        return ((Task) ((PriorityProvider) this.getDelegate())).isFinished();
    }

    public void setError(Throwable throwable) {
        ((Task) ((PriorityProvider) this.getDelegate())).setError(throwable);
    }

    public Throwable getError() {
        return ((Task) ((PriorityProvider) this.getDelegate())).getError();
    }

    public <T extends Dependency<Task> & PriorityProvider & Task> T getDelegate() {
        return (Dependency) this.delegate;
    }

    protected <T extends Dependency<Task> & PriorityProvider & Task> T checkAndInitDelegate(
            Object object) {
        return (Dependency) (PriorityTask.isProperDelegate(object) ? (Dependency) object
                : new PriorityTask());
    }
}
