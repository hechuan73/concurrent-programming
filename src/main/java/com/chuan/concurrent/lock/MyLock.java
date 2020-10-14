package com.chuan.concurrent.lock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * An exclusive unfair lock implement by AQS.
 *
 * @author hechuan
 */
public class MyLock implements Lock {

    private final Sync sync;

    public MyLock() {
        sync = new Sync();
    }

    @Override
    public void lock() {
        sync.acquire(1);
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        sync.acquireInterruptibly(1);
    }

    @Override
    public boolean tryLock() {
        return sync.tryAcquire(1);
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return sync.tryAcquireNanos(1, unit.toNanos(time));
    }

    @Override
    public void unlock() {
        sync.release(1);
    }

    @Override
    public Condition newCondition() {
        return sync.newCondition();
    }

    public boolean isLocked() {
        return sync.isHeldExclusively();
    }

    public boolean hasQueuedThreads() {
        return sync.hasQueuedThreads();
    }

    private static final class Sync extends AbstractQueuedSynchronizer {
        protected Condition newCondition() { return new ConditionObject(); }

        @Override
        protected boolean tryAcquire(int arg) {
            if (compareAndSetState(0, arg)) {
                setExclusiveOwnerThread(Thread.currentThread());
                return true;
            }
            return false;
        }

        @Override
        protected boolean isHeldExclusively() {
            return getState() == 1;
        }

        @Override
        protected boolean tryRelease(int arg) {
            if (getState() == 0) { throw new IllegalStateException(); }
            setState(0);
            setExclusiveOwnerThread(null);
            return true;
        }
    }

}
