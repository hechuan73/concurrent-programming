package com.chuan.concurrent.multithreads;

import java.util.concurrent.ThreadFactory;

/**
 * @author hechuan
 */
public class MyThreadFactory implements ThreadFactory {


    @Override
    public Thread newThread(Runnable r) {
        return new Thread(r);
    }
}
