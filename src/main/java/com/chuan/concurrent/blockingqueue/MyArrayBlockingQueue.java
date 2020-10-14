package com.chuan.concurrent.blockingqueue;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author hechuan
 */
public class MyArrayBlockingQueue<E> {

    private final Object[] data;
    private final ReentrantLock lock;
    private final Condition notEmpty;
    private final Condition notFull;
    private int count;
    private int putIndex;
    private int takeIndex;

    public MyArrayBlockingQueue(int size, boolean fair) {
        data = new Object[size];
        lock = new ReentrantLock(fair);
        notEmpty = lock.newCondition();
        notFull = lock.newCondition();
    }

    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
        if (e == null) { throw new IllegalStateException(); }
        long nanoTimes = unit.toNanos(timeout);
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            while (count == data.length) {
                if (nanoTimes <= 0L) { return false; }
                nanoTimes = notFull.awaitNanos(nanoTimes);
            }
            enqueue(e);
            return true;
        } finally {
            lock.unlock();
        }
    }

    public boolean offer(E e) {
        if (e == null) { throw new IllegalStateException(); }
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            if (data.length == count) { return false; }
            enqueue(e);
            return true;
        } finally {
            lock.unlock();
        }
    }

    private void enqueue(E e) {
        final Object[] item = this.data;
        item[putIndex++] = e;
        if (putIndex == item.length) { putIndex = 0; }
        count++;
        notEmpty.signalAll();
    }

    public E poll() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return count == 0 ? null : dequeue();
        } finally {
            lock.unlock();
        }
    }

    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        long nanoTimes = unit.toNanos(timeout);
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            while (count == 0) {
                if (nanoTimes <= 0L) { return null; }
                nanoTimes = notEmpty.awaitNanos(nanoTimes);
            }
            return dequeue();
        } finally {
            lock.unlock();
        }
    }

    private E dequeue() {
        final Object[] item = this.data;
        @SuppressWarnings("unchecked")
        E e = (E) item[takeIndex];
        item[takeIndex++] = null;
        if (takeIndex == item.length) { takeIndex = 0; }
        notFull.signalAll();
        count--;
        return e;
    }
}
