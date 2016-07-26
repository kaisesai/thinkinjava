package com.liukai.thinkinjava.concurrent;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * CountDownLatch示例
 * @author Administrator
 *
 */
public class CountDownLatchDemo {

	public static void main(String[] args) throws InterruptedException {
		int size = 100;
		ExecutorService es = Executors.newCachedThreadPool();
		CountDownLatch countDownLatch = new CountDownLatch(size);
		for (int i = 0; i < 10; i++) {
			es.execute(new WaitingTask(countDownLatch));
		}

		for (int i = 0; i < size; i++) {
			es.execute(new TaskPortion(countDownLatch));
		}

		TimeUnit.SECONDS.sleep(5);
		es.shutdownNow();
	}
}

class WaitingTask implements Runnable {

	private static int counter = 0;
	private final int id = counter++;
	private CountDownLatch latch;

	public WaitingTask(CountDownLatch countDownLatch) {
		this.latch = countDownLatch;
	}

	@Override
	public void run() {
		try {
			latch.await();
			System.out.println("完成等待 " + this);
		} catch (InterruptedException e) {
			System.out.println(this + "中断了...");
		}
	}

	@Override
	public String toString() {
		return String.format("WaitingTask  %1$-3d", id).toString();
	}
}

class TaskPortion implements Runnable {

	private static int counter = 0;
	private final int id = counter++;
	private static Random random = new Random();
	private CountDownLatch latch;

	public TaskPortion(CountDownLatch countDownLatch) {
		this.latch = countDownLatch;
	}

	public void doWork() throws InterruptedException {
		TimeUnit.MILLISECONDS.sleep(random.nextInt(2000));
		System.out.println(this + " 完成任务...");
	}

	@Override
	public void run() {
		try {
			doWork();
			latch.countDown();
		} catch (InterruptedException e) {
			System.out.println(id + " 任务中断了...");
		}
	}

	@Override
	public String toString() {
		return String.format("%1$-3d", id).toString();
	}
}
