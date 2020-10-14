package com.chuan.concurrent.lock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * An shared unfair lock implement by AQS.
 *
 * @author hechuan
 */
public class MySharedLock implements Lock {

    private final Sync sync;

    public MySharedLock() {
        this.sync = new Sync(2);
    }


    @Override
    public void lock() {
        sync.acquireShared(1);
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        sync.acquireSharedInterruptibly(1);
    }

    @Override
    public boolean tryLock() {
        return sync.tryAcquireShared(1) >= 0;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return sync.tryAcquireSharedNanos(1, unit.toNanos(time));
    }

    @Override
    public void unlock() {
        sync.releaseShared(1);
    }

    @Override
    public Condition newCondition() {
        return sync.newCondition();
    }

    private static class Sync extends AbstractQueuedSynchronizer {

        public Sync(int arg) {
            if (arg <= 0) { throw new IllegalStateException("Initial state must be large than 0!"); }
            setState(2);
        }

        @Override
        protected int tryAcquireShared(int arg) {
            for (;;) {
                int state = getState();
                int newState = state - arg;
                // if newState < 0, the caller acquireShared() will call doAcquireShared() to queue and block it,
                // otherwise do CAS.
                if (newState < 0 || compareAndSetState(state, newState)) { return newState; }
            }
        }

        @Override
        protected boolean tryReleaseShared(int arg) {
            for (;;) {
                int state = getState();
                int newState = state + arg;
                if (compareAndSetState(state, newState)) { return true; }
            }
        }

        protected Condition newCondition() {
            return new ConditionObject();
        }
    }

}
