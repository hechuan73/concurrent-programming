package com.chuan.concurrent.multithreads;

import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 控制线程顺序执行的8种方式
 * 1. 使用线程的join方法
 * 2. 使用主线程的join方法
 * 3. 使用线程的wait方法
 * 4. 使用线程的线程池方法
 * 5. 使用线程的Condition(条件变量)方法
 * 6. 使用线程的CountDownLatch(倒计数)方法
 * 7. 使用线程的CyclicBarrier(回环栅栏)方法
 * 8. 使用线程的Semaphore(信号量)方法
 *
 * 场景：
 * 1.早上；
 * 2.测试人员、产品经理、开发人员陆续的来公司上班；
 * 3.产品经理规划新需求；
 * 4.开发人员开发新需求功能；
 * 5.测试人员测试新功能。
 *
 * 规划需求，开发需求新功能，测试新功能是一个有顺序的，我们把thread1看做产品经理，thread2看做开发人员，thread3看做测试人员。
 *
 * @author hechuan
 */
public class ThreadsRunInOrder {

    /**
     * 1. 使用线程的join()方法
     */
    private void controlWithJoin() {
        final Thread t1 = new Thread(() -> System.out.println("产品经理规划新需求！"));
        final Thread t2 = new Thread(() -> {
            try {
                t1.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("开发人员开发新需求功能！");
        });
        final Thread t3 = new Thread(() -> {
            try {
                t2.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("测试人员测试新功能！");
        });

        System.out.println("早上：");
        System.out.println("测试人员来上班了...");
        t3.start();
        System.out.println("产品经理来上班了...");
        t1.start();
        System.out.println("开发人员来上班了...");
        t2.start();
    }

    /**
     * 2. 使用主线程的join()方法
     */
    private void controlWithMainThreadJoin() throws InterruptedException {
        final Thread t1 = new Thread(() -> System.out.println("产品经理规划新需求！"));
        final Thread t2 = new Thread(() -> System.out.println("开发人员开发新需求功能！"));
        final Thread t3 = new Thread(() -> System.out.println("测试人员测试新功能！"));

        System.out.println("早上:");
        System.out.println("产品经理来上班了");
        System.out.println("测试人员来上班了");
        System.out.println("开发人员来上班了");
        t1.start();
        //在父进程调用子进程的join()方法后，父进程需要等待子进程运行完再继续运行。
        System.out.println("开发人员和测试人员休息会...");
        t1.join();
        System.out.println("产品经理新需求规划完成!");
        t2.start();
        System.out.println("测试人员休息会...");
        t2.join();
        t3.start();
    }

    private static Object myLock1 = new Object();
    private static Object myLock2 = new Object();

    /**
     * 如果没有状态标识，当t1已经运行完了t2才运行，t2在等待t1唤醒导致t2永远处于等待状态
     */
    private static boolean t1Run = false;
    private static boolean t2Run = false;

    /**
     * 3. 使用线程的wait方法
     * 这里其实采用的是生产者和消费者的模式，不过不是循环消费。在生产者-消费者的经典范式写法中，在wait返回之后，一般还需要再检查一次：
     * synchronized (myLock1) {
     *     while (!t1Run) {
     *         myLock1.wait();
     *     }
     *     ...
     * }
     */
    private void controlWithThreadWait() {
        final Thread t1 = new Thread(() -> {
            synchronized (myLock1) {
                System.out.println("产品经理规划新需求...");
                t1Run = true;
                myLock1.notifyAll();
            }
        });

        final Thread t2 = new Thread(() -> {
            synchronized (myLock1) {
                try {
                    if (!t1Run) {
                        System.out.println("开发人员先休息会...");
                        myLock1.wait();
                    }

                    synchronized (myLock2) {
                        System.out.println("开发人员开发新需求功能");
                        t2Run = true;
                        myLock2.notifyAll();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        final Thread t3 = new Thread(() -> {
            synchronized (myLock2) {
                try {
                    if (!t2Run) {
                        System.out.println("测试人员先休息会...");
                        myLock2.wait();
                    }

                    System.out.println("测试人员测试新功能");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        });

        System.out.println("早上：");
        System.out.println("测试人员来上班了...");
        t3.start();
        System.out.println("开发人员来上班了...");
        t2.start();
        System.out.println("产品经理来上班了...");
        t1.start();
    }

    /**
     * 4. 使用线程的线程池方法
     *
     * 使用Executors类提供的newSingleThreadExecutor()，它是一个单线程化的线程池，支持串行执行任务。
     */
    @SuppressWarnings("AlibabaThreadPoolCreation")
    private void controlWithThreadPool() {
        final Thread t1 = new Thread(() -> System.out.println("产品经理规划新需求！"));
        final Thread t2 = new Thread(() -> System.out.println("开发人员开发新需求功能！"));
        final Thread t3 = new Thread(() -> System.out.println("测试人员测试新功能！"));

        // Alibaba does not recommend to create thread by Executors.
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        System.out.println("早上：");
        System.out.println("产品经理来上班了");
        System.out.println("测试人员来上班了");
        System.out.println("开发人员来上班了");
        System.out.println("领导吩咐:");
        System.out.println("首先，产品经理规划新需求...");
        executorService.submit(t1);
        System.out.println("然后，开发人员开发新需求功能...");
        executorService.submit(t2);
        System.out.println("最后，测试人员测试新功能...");
        executorService.submit(t3);
        executorService.shutdown();
    }


    private static final Lock lock = new ReentrantLock();
    private static final Condition condition1 = lock.newCondition();
    private static final Condition condition2 = lock.newCondition();

    /**
     * 5. 使用线程的Condition（条件变量）方法
     *
     * 通过使用Lock创建不同的condition，然后不通的线程等待不同的condition，以此形成串行执行。
     */
    private void controlWithCondition() {
        final Thread t1 = new Thread(() -> {
            lock.lock();
            System.out.println("产品经理规划新需求");
            t1Run = true;
            condition1.signalAll();
            lock.unlock();
        });

        final Thread t2 = new Thread(() -> {
            lock.lock();

            try {
                if (!t1Run) {
                    System.out.println("开发人员先休息会...");
                    condition1.await();
                }

                System.out.println("开发人员开发新需求功能");
                t2Run = true;
                condition2.signalAll();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            lock.unlock();
        });

        final Thread t3 = new Thread(() -> {
            lock.lock();
            try {
                if (!t2Run) {
                    System.out.println("测试人员先休息会...");
                    condition1.await();
                }

                System.out.println("测试人员测试新功能");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            lock.unlock();
        });

        System.out.println("早上：");
        System.out.println("测试人员来上班了...");
        t3.start();
        System.out.println("产品经理来上班了...");
        t1.start();
        System.out.println("开发人员来上班了...");
        t2.start();
    }

    /**
     * 标识线程1, 2分别是否执行
     */
    private static final CountDownLatch countDownLatch1 = new CountDownLatch(1);
    private static final CountDownLatch countDownLatch2 = new CountDownLatch(1);
    /**
     * 6. 使用线程的countDownLatch（计数器）方法
     *
     * 通过为每个线程创建计数器，来保证顺序执行关系。注意：await()方法不会阻塞线程，自旋等待。
     */
    private void controlWithCountDownLatch() {
        final Thread t1 = new Thread(() -> {
            System.out.println("产品经理规划新需求");
            countDownLatch1.countDown();
        });

        final Thread t2 = new Thread(() -> {
            try {
                countDownLatch1.await();
                System.out.println("开发人员开发新需求功能");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // 保证countDown()在线程退出前一定会执行，如果放在try中，发生异常有可能执行不到
            countDownLatch2.countDown();
        });

        final Thread t3 = new Thread(() -> {
            try {
                countDownLatch2.await();
                System.out.println("测试人员测试新功能");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        System.out.println("早上：");
        System.out.println("测试人员来上班了...");
        t3.start();
        System.out.println("产品经理来上班了...");
        t1.start();
        System.out.println("开发人员来上班了...");
        t2.start();
    }

    private static final CyclicBarrier barrier1 = new CyclicBarrier(2);
    private static final CyclicBarrier barrier2 = new CyclicBarrier(2);
    /**
     * 7. 使用CyclicBarrier（回环栏栅）实现线程顺序执行
     *
     * CyclicBarrier可以设置一组栏栅，等所有线程到达某个状态之后才可以执行。
     * 在线程2执行之前，设置两道屏障，表示线程1到达且执行完，线程2 到达且可以执行；同理在线程3执行之前，设置两道屏障，表示线程2到达且执行完，线
     * 程3 到达且可以执行。
     */
    private void controlWithCyclicBarrier() {
        final Thread t1 = new Thread(() -> {
            System.out.println("产品经理规划新需求");
            try {
                // 放开栏栅1
                barrier1.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }
        });

        final Thread t2 = new Thread(() -> {
            try {
                // 放开栏栅1
                barrier1.await();
                System.out.println("开发人员开发新需求功能");
                // 放开栏栅2
                barrier2.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }
        });

        final Thread t3 = new Thread(() -> {
            try {
                // 放开栏栅2
                barrier2.await();
                System.out.println("测试人员测试新功能");
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }
        });

        System.out.println("早上：");
        System.out.println("测试人员来上班了...");
        t3.start();
        System.out.println("产品经理来上班了...");
        t1.start();
        System.out.println("开发人员来上班了...");
        t2.start();
    }

    private static final Semaphore SEMAPHORE1 = new Semaphore(1);
    private static final Semaphore SEMAPHORE2 = new Semaphore(1);
    private static final Semaphore SEMAPHORE3 = new Semaphore(1);

    /**
     * 8. 使用线程的信号量机制实现线程顺序执行。
     * 使用三个信号量，进行循环控制。
     */
    private void controlWithSemaphore() throws InterruptedException {
        final Thread t1 = new Thread(() -> {
            try {
                SEMAPHORE1.acquire();
                System.out.println("产品经理规划新需求");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            SEMAPHORE2.release();
        });

        final Thread t2 = new Thread(() -> {
            try {
                SEMAPHORE2.acquire();
                System.out.println("开发人员开发新需求功能");
                SEMAPHORE3.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        final Thread t3 = new Thread(() -> {
            try {
                SEMAPHORE3.acquire();
                System.out.println("测试人员测试新功能");
                SEMAPHORE1.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        SEMAPHORE2.acquire();
        SEMAPHORE3.acquire();
        System.out.println("早上：");
        System.out.println("测试人员来上班了...");
        t3.start();
        System.out.println("开发人员来上班了...");
        t2.start();
        System.out.println("产品经理来上班了...");
        t1.start();

    }
}
