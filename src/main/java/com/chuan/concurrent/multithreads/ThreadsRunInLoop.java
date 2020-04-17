package com.chuan.concurrent.multithreads;

import java.util.concurrent.ThreadFactory;

/**
 * @author hechuan
 */
public class ThreadsRunInLoop {

    static final Object OBJ1 = new Object();
    static final Object OBJ2 = new Object();
    static final Object OBJ3 = new Object();
    static boolean t3Run = true, t2Run = false, t1Run = false;

    static volatile int count = 1;
    public static void main(String[] args) {
        ThreadFactory factory = new MyThreadFactory();

        threadsRunInOrderLoopWithWaitNotify(10, factory);
    }

    public static void threadsRunInOrderLoopWithWaitNotify(int times, ThreadFactory factory) {
        Thread t1 = factory.newThread(() -> {
            int time = times;
            while (time != 0) {
                synchronized (OBJ3) {
                    while (!t3Run) {
                        try {
                            OBJ3.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    System.out.println(1);
                    t3Run = false;
                    t1Run = true;
                    synchronized (OBJ1) {
                        OBJ1.notify();
                    }
                    time--;
                }
            }
        });
        Thread t2 = factory.newThread(() -> {
            int time = times;
            while (time != 0) {
                synchronized (OBJ1) {
                    while (!t1Run) {
                        try {
                            OBJ1.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    System.out.println(2);
                    t1Run = false;
                    t2Run = true;
                    synchronized (OBJ2) {
                        OBJ2.notify();
                    }
                    time--;
                }
            }
        });
        Thread t3 = factory.newThread(() -> {
            int time = times;
            while (time != 0) {
                synchronized (OBJ2) {
                    while (!t2Run) {
                        try {
                            OBJ2.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    System.out.println(3);
                    t2Run = false;
                    t3Run = true;
                    synchronized (OBJ3) {
                        OBJ3.notify();
                    }
                    time--;
                }
            }
        });
        t1.start();
        t2.start();
        t3.start();
    }

    public static void threadsRunInOrderLoopWithVolatile(int times, ThreadFactory factory) {
        Thread t1 = factory.newThread(() -> {
            int time = times;
            while (time != 0) {
                if (count == 1) {
                    System.out.println(1);
                    count = 2;
                    time--;
                }
            }
        });
        Thread t2 = factory.newThread(() -> {
            int time = times;
            while (time != 0) {
                if (count == 2) {
                    System.out.println(2);
                    count = 3;
                    time--;
                }

            }
        });
        Thread t3 = factory.newThread(() -> {
            int time = times;
            while (time != 0) {
                if (count == 3) {
                    System.out.println(3);
                    count = 1;
                    time--;
                }
            }
        });
        t1.start();
        t2.start();
        t3.start();
    }

}
