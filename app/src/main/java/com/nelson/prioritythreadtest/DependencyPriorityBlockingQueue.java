package com.nelson.prioritythreadtest;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Nelson on 2017/12/4.
 */

public class DependencyPriorityBlockingQueue<E extends Dependency & Task & PriorityProvider> extends
        PriorityBlockingQueue<E> {

    static final int TAKE = 0;
    static final int PEEK = 1;
    static final int POLL = 2;
    static final int POLL_WITH_TIMEOUT = 3;
    final Queue<E> blockedQueue = new LinkedList();
    private final ReentrantLock lock = new ReentrantLock();

    public DependencyPriorityBlockingQueue() {
    }

    public E take() throws InterruptedException {
        return this.get(0, (Long) null, (TimeUnit) null);
    }

    public E peek() {
        try {
            return this.get(1, (Long) null, (TimeUnit) null);
        } catch (InterruptedException var2) {
            return null;
        }
    }

    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        return this.get(3, Long.valueOf(timeout), unit);
    }

    public E poll() {
        try {
            return this.get(2, (Long) null, (TimeUnit) null);
        } catch (InterruptedException var2) {
            return null;
        }
    }

    public int size() {
        int var1;
        try {
            this.lock.lock();
            var1 = this.blockedQueue.size() + super.size();
        } finally {
            this.lock.unlock();
        }

        return var1;
    }

    public <T> T[] toArray(T[] a) {
        Object[] var2;
        try {
            this.lock.lock();
            var2 = this.concatenate(super.toArray(a), this.blockedQueue.toArray(a));
        } finally {
            this.lock.unlock();
        }

        return var2;
    }

    public Object[] toArray() {
        Object[] var1;
        try {
            this.lock.lock();
            var1 = this.concatenate(super.toArray(), this.blockedQueue.toArray());
        } finally {
            this.lock.unlock();
        }

        return var1;
    }

    public int drainTo(Collection<? super E> c) {
        try {
            this.lock.lock();
            int numberOfItems = super.drainTo(c) + this.blockedQueue.size();

            while (!this.blockedQueue.isEmpty()) {
                c.add(this.blockedQueue.poll());
            }

            int var3 = numberOfItems;
            return var3;
        } finally {
            this.lock.unlock();
        }
    }

    public int drainTo(Collection<? super E> c, int maxElements) {
        try {
            this.lock.lock();

            int numberOfItems;
            for (numberOfItems = super.drainTo(c, maxElements);
                    !this.blockedQueue.isEmpty() && numberOfItems <= maxElements; ++numberOfItems) {
                c.add(this.blockedQueue.poll());
            }

            int var4 = numberOfItems;
            return var4;
        } finally {
            this.lock.unlock();
        }
    }

    public boolean contains(Object o) {
        boolean var2;
        try {
            this.lock.lock();
            var2 = super.contains(o) || this.blockedQueue.contains(o);
        } finally {
            this.lock.unlock();
        }

        return var2;
    }

    public void clear() {
        try {
            this.lock.lock();
            this.blockedQueue.clear();
            super.clear();
        } finally {
            this.lock.unlock();
        }

    }

    public boolean remove(Object o) {
        boolean var2;
        try {
            this.lock.lock();
            var2 = super.remove(o) || this.blockedQueue.remove(o);
        } finally {
            this.lock.unlock();
        }

        return var2;
    }

    public boolean removeAll(Collection<?> collection) {
        boolean var2;
        try {
            this.lock.lock();
            var2 = super.removeAll(collection) | this.blockedQueue.removeAll(collection);
        } finally {
            this.lock.unlock();
        }

        return var2;
    }

    E performOperation(int operation, Long time, TimeUnit unit) throws InterruptedException {
        Dependency value;
        switch (operation) {
            case 0:
                value = (Dependency) super.take();
                break;
            case 1:
                value = (Dependency) super.peek();
                break;
            case 2:
                value = (Dependency) super.poll();
                break;
            case 3:
                value = (Dependency) super.poll(time.longValue(), unit);
                break;
            default:
                return null;
        }

        return value;
    }

    boolean offerBlockedResult(int operation, E result) {
        boolean var3;
        try {
            this.lock.lock();
            if (operation == 1) {
                super.remove(result);
            }

            var3 = this.blockedQueue.offer(result);
        } finally {
            this.lock.unlock();
        }

        return var3;
    }

    E get(int operation, Long time, TimeUnit unit) throws InterruptedException {
        Dependency result;
        while ((result = this.performOperation(operation, time, unit)) != null && !this
                .canProcess(result)) {
            this.offerBlockedResult(operation, result);
        }

        return result;
    }

    boolean canProcess(E result) {
        return result.areDependenciesMet();
    }

    public void recycleBlockedQueue() {
        try {
            this.lock.lock();
            Iterator iterator = this.blockedQueue.iterator();

            while (iterator.hasNext()) {
                E blockedItem = (Dependency) iterator.next();
                if (this.canProcess(blockedItem)) {
                    super.offer(blockedItem);
                    iterator.remove();
                }
            }
        } finally {
            this.lock.unlock();
        }

    }

    <T> T[] concatenate(T[] arr1, T[] arr2) {
        int arr1Len = arr1.length;
        int arr2Len = arr2.length;
        T[] C = (Object[])((Object[])Array.newInstance(arr1.getClass().getComponentType(), arr1Len + arr2Len));
        System.arraycopy(arr1, 0, C, 0, arr1Len);
        System.arraycopy(arr2, 0, C, arr1Len, arr2Len);
        return C;
    }
}

