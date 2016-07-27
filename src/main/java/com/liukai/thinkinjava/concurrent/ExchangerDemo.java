package com.liukai.thinkinjava.concurrent;

import java.util.List;
import java.util.concurrent.*;

/**
 * Exchanger是两个任务之间交换对象的栅栏，
 * Created by Administrator on 2016/7/28 0028.
 */
public class ExchangerDemo {

    public static final int SIZE = 20;

    public static void main(String[] args) throws InterruptedException {
        Exchanger<List<Fat>> exchanger = new Exchanger<>();
        ExecutorService es = Executors.newCachedThreadPool();
        es.execute(new ExchangeProducer<>(Fat.class, exchanger, new CopyOnWriteArrayList<Fat>()));
        es.execute(new ExchangerConsumer<>(exchanger, new CopyOnWriteArrayList<Fat>()));
        TimeUnit.SECONDS.sleep(1);
        es.shutdownNow();
    }
}

/**
 * 生产者任务，用于生成对象并放入到List中并通过Exchanger对象交换到消费者线程
 *
 * @param <T>
 */
class ExchangeProducer<T> implements Runnable {

    private Exchanger<List<T>> exchanger;
    private List<T> holder;
    private Class<T> clazz;

    ExchangeProducer(Class<T> clazz, Exchanger<List<T>> exchanger, List<T> holder) {
        this.exchanger = exchanger;
        this.holder = holder;
        this.clazz = clazz;
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                for (int i = 0; i < ExchangerDemo.SIZE; i++) {
                    try {
                        holder.add(i, clazz.newInstance());
                    } catch (Exception e) {
                        System.out.println("类创建失败：" + e.getMessage());
                    }
                }
                holder = exchanger.exchange(holder);
                System.out.println("生产者线程：交换过后的holder对象为：" + holder);
            }
        } catch (InterruptedException e) {
            System.out.println(Thread.currentThread().getName() + "：当前线程被中断");
        }
    }
}

/**
 * 消费者线程，通过Exchanger对象来从生产者那里交换List对象并消费其中的对象
 *
 * @param <T>
 */
class ExchangerConsumer<T> implements Runnable {

    private Exchanger<List<T>> exchanger;
    private List<T> holder;
    private volatile T value;

    ExchangerConsumer(Exchanger<List<T>> exchanger, List<T> holder) {
        this.exchanger = exchanger;
        this.holder = holder;
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                holder = exchanger.exchange(holder);
                System.out.println("消费者线程：交换过后的holder对象为：" + holder);
                for (T x : holder) {
                    value = x;
                    holder.remove(x);
                }
            }
        } catch (InterruptedException e) {
            System.out.println(Thread.currentThread().getName() + "：当前线程被中断");
        }
        System.out.println("最后一个对象为：" + value);
    }
}