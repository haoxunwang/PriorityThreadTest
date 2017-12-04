package com.nelson.prioritythreadtest;

/**
 * Created by Nelson on 2017/12/4.
 */

public interface DelegateProvider {

    <T extends Dependency<Task> & PriorityProvider & Task> T getDelegate();
}

