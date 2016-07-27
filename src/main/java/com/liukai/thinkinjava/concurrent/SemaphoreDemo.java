package com.liukai.thinkinjava.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * 计数信号量允许n个任务同时访问这个资源。还可以将信号量看做是使用资源的“许可证”
 * Created by Administrator on 2016/7/26 0026.
 */
public class SemaphoreDemo {


    public static void main(String[] args) throws InterruptedException {
        //创建对象池
        int size = 20;
        final Pool<Fat> pool = new Pool<>(Fat.class, size);

        //创建签出任务并执行
        ExecutorService es = Executors.newCachedThreadPool();
        for (int i = 0; i < size; i++) {
            es.execute(new CheckOutTask<>(pool));
        }
        System.out.println("All CheckOutTasks created");

        //从对象池中签出对象进行操作并保存到list中
        List<Fat> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Fat fat = pool.checkOut();
            System.out.println(i + ": main() thread checked out ");
            fat.operation();
            list.add(fat);
        }

        //执行一个可执行的任务
        Future<?> blocked = es.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    pool.checkOut();
                } catch (InterruptedException e) {
                    System.out.println("checkOut() Interrupted");
                }
            }
        });
        TimeUnit.SECONDS.sleep(2);
        blocked.cancel(true);//打断其调用
        System.out.println("checking in objects in " + list);

        //从对象池中签入
        for (Fat fat : list) {
            pool.checkIn(fat);
        }
        //从对象池中再次签入
        for (Fat fat : list) {
            pool.checkIn(fat);
        }

        //中断所有线程
        es.shutdownNow();
    }

}


/**
 * 签出任务，该任务将签出Fat对象，持有一段时间后将其签回。
 *
 * @param <T>
 */
class CheckOutTask<T> implements Runnable {

    private static int counter = 0;
    private final int id = counter++;
    private Pool<T> pool;

    public CheckOutTask(Pool<T> pool) {
        this.pool = pool;
    }

    @Override
    public void run() {
        try {
            T item = pool.checkOut();
            System.out.println(this + " checked out " + item);
            TimeUnit.SECONDS.sleep(10);
            System.out.println(this + " checked in " + item);
            pool.checkIn(item);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return Thread.currentThread().getName() + " CheckOutTask id: " + id;
    }
}

/**
 * 创建代价很高的对象类型
 */
class Fat {

    private volatile double d;
    private static int counter = 0;
    private final int id = counter++;

    public Fat() {
        for (int i = 0; i < 10000; i++) {
            d += (Math.PI + Math.E) / d;
        }
    }

    public void operation() {
        System.out.println(this);
    }

    @Override
    public String toString() {
        return "Fat id: " + id;
    }
}


/**
 * 对象池
 * 通过Semaphore技术信号量对象来管理对象的签入签出
 *
 * @param <T>
 */
class Pool<T> {

    private int size;
    private List<T> items = new ArrayList<>();
    private volatile boolean[] checkOut;
    private Semaphore available;//计数信号量

    public Pool(Class<T> classObject, int size) {
        this.available = new Semaphore(size, true);
        this.size = size;
        this.checkOut = new boolean[size];
        //初始化对象池可以被签出的对象
        for (int i = 0; i < size; i++) {
            try {
                //确保T的具有公共的无参构造器
                items.add(i, classObject.newInstance());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 签出对象
     *
     * @return
     * @throws InterruptedException
     */
    public T checkOut() throws InterruptedException {
        available.acquire();
        return getItem();
    }

    /**
     * 签入，使用完对象后签入对象到池中
     *
     * @param x
     */
    public void checkIn(T x) {
        if (realseItem(x)) {
            available.release();
        }
    }


    /**
     * 获取对象
     *
     * @return
     */
    private synchronized T getItem() {
        for (int i = 0; i < size; i++) {
            if (!checkOut[i]) {
                checkOut[i] = true;
                return items.get(i);
            }
        }
        return null;
    }

    /**
     * 释放对象
     *
     * @param item
     * @return
     */
    private synchronized boolean realseItem(T item) {
        int index = items.indexOf(item);
        if (index == -1) {
            return false;
        }
        if (checkOut[index]) {
            checkOut[index] = false;
            return true;
        }
        return false;
    }

}
