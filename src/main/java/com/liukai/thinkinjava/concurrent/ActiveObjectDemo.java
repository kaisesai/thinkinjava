package com.liukai.thinkinjava.concurrent;

import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

/**
 * 活动对象或行动者
 * 每个对象都维护这它自己的工作器线程和消息队列，并且所有对这种对象的请求都将进入队列排队，任何时刻都只能运行其中的一个。
 * Created by Administrator on 2016/8/9 0009.
 */
public class ActiveObjectDemo {

    private ExecutorService es = Executors.newSingleThreadExecutor();
    private Random random = new Random(47);

    public static void main(String[] args) {

        ActiveObjectDemo aod = new ActiveObjectDemo();
        List<Future<?>> results = new CopyOnWriteArrayList<>();
        for (float f = 0.0f; f < 1.0f; f += 0.2f) {
            results.add(aod.calculateFloat(f, f));
        }
        for (int i = 0; i < 5; i++) {
            results.add(aod.calculateInt(i, i));
        }
        System.out.println("All asych calls made");
        while (results.size() > 0) {
            for (Future f : results) {
                if (f.isDone()) {
                    try {
                        System.out.println(f.get());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    results.remove(f);
                }
            }
        }
        aod.shutdown();
    }

    /**
     * 暂停，间歇
     *
     * @param factor
     */
    private void pause(int factor) {
        try {
            TimeUnit.MILLISECONDS.sleep(100 + random.nextInt(factor));
        } catch (InterruptedException e) {
            System.out.println("sleep() interrupted");
        }
    }

    public Future<Integer> calculateInt(final int x, final int y) {
        return es.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                System.out.println("starting " + x + " + " + y);
                pause(500);
                return x + y;
            }
        });
    }

    public Future<Float> calculateFloat(final float x, final float y) {
        return es.submit(new Callable<Float>() {
            @Override
            public Float call() throws Exception {
                System.out.println("starting " + x + " + " + y);
                pause(2000);
                return x + y;
            }
        });
    }

    public void shutdown() {
        es.shutdown();
    }

}
