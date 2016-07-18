package com.liukai.thinkinjava.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class WaxOMatic {

	public static void main(String[] args) throws InterruptedException {
		Car car = new Car();
		ExecutorService es = Executors.newCachedThreadPool();
		es.execute(new WaxOn(car));
		es.execute(new WaxOff(car));
		TimeUnit.SECONDS.sleep(1);
		es.shutdownNow();
	}
}

/**
 * 抛光任务
 * 
 * @author Administrator
 *
 */
class WaxOff implements Runnable {

	private Car car;

	public WaxOff(Car car) {
		this.car = car;
	}

	@Override
	public void run() {
		try {
			while (!Thread.interrupted()) {
				car.waitForWaxing();
				System.out.println("抛光！");
				// TimeUnit.SECONDS.sleep(1);
				car.buffed();
			}
		} catch (InterruptedException e) {
			System.out.println(this.toString() + "：" + e);
		}
		System.out.println("抛光任务结束");
	}

	@Override
	public String toString() {
		return "抛光任务";
	}
}

/**
 * 打蜡任务
 * 
 * @author Administrator
 *
 */
class WaxOn implements Runnable {

	private Car car;

	public WaxOn(Car car) {
		this.car = car;
	}

	@Override
	public void run() {
		try {
			while (!Thread.interrupted()) {
				System.out.println("打蜡！");
				// TimeUnit.SECONDS.sleep(1);
				car.waxed();
				car.waitForBuffing();
			}
		} catch (InterruptedException e) {
			System.out.println(this.toString() + "：" + e);
		}
		System.out.println("打蜡任务结束");
	}

	@Override
	public String toString() {
		return "打蜡任务";
	}
}

/**
 * 小汽车。 功能有：打蜡、抛光
 * 
 * @author Administrator
 *
 */
class Car {

	private boolean waxOn = false;// 打蜡状态

	/**
	 * 打蜡
	 */
	public synchronized void waxed() {
		waxOn = true;
		this.notifyAll();
	}

	/**
	 * 抛光
	 */
	public synchronized void buffed() {
		waxOn = false;
		this.notifyAll();
	}

	/**
	 * 等待打蜡 由抛光任务调用
	 * 
	 * @throws InterruptedException
	 */
	public synchronized void waitForWaxing() throws InterruptedException {
		// while (waxOn == false) {
		if (waxOn == false) {
			this.wait();
		}
	}

	/**
	 * 等待抛光 由打蜡任务调用
	 * 
	 * @throws InterruptedException
	 */
	public synchronized void waitForBuffing() throws InterruptedException {
		// while (waxOn == true) {
		if (waxOn == true) {
			this.wait();
		}
	}

}
