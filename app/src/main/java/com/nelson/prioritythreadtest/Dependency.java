package com.nelson.prioritythreadtest;

import java.util.Collection;

/**
 * Created by Nelson on 2017/12/4.
 */

public interface Dependency<T> {

    void addDependency(T var1);

    Collection<T> getDependencies();

    boolean areDependenciesMet();
}
