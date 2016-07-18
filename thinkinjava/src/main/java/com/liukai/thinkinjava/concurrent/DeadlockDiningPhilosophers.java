package com.liukai.thinkinjava.concurrent;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 死锁
 * 
 * @author Administrator
 *
 */
public class DeadlockDiningPhilosophers {

	public static void main(String[] args) throws InterruptedException {

		int ponder = 5;
		int size = 5;
		Chopstick[] chopsticks = new Chopstick[size];
		for (int i = 0; i < size; i++) {
			chopsticks[i] = new Chopstick();
		}

		ExecutorService es = Executors.newCachedThreadPool();
		for (int i = 0; i < size; i++) {
			if (i == (size - 1)) {
				es.execute(new Philosopher(chopsticks[0], chopsticks[i], i, ponder));
			} else {
				es.execute(new Philosopher(chopsticks[i], chopsticks[i + 1], i, ponder));
			}
			// es.execute(new Philosopher(chopsticks[i], chopsticks[(i + 1) %
			// size], i, ponder));
		}

		TimeUnit.SECONDS.sleep(5);

		es.shutdownNow();

	}
}

/**
 * 哲学家
 * 
 * @author Administrator
 *
 */
class Philosopher implements Runnable {

	private Chopstick left;
	private Chopstick right;
	private final int id;
	private final int ponderFactor;
	private Random random = new Random(47);

	public Philosopher(Chopstick left, Chopstick right, int id, int ponderFactor) {
		this.left = left;
		this.right = right;
		this.id = id;
		this.ponderFactor = ponderFactor;
	}

	/**
	 * 思考
	 * 
	 * @throws InterruptedException
	 */
	public void pause() throws InterruptedException {
		TimeUnit.MILLISECONDS.sleep(random.nextInt(ponderFactor * 250));
	}

	@Override
	public void run() {
		try {
			while (!Thread.interrupted()) {
				System.out.println(this + " 思考");
				pause();
				System.out.println(this + " 拿走右边的筷子");
				right.take();
				System.out.println(this + " 拿走左边的筷子");
				left.take();
				System.out.println(this + " 开始进餐");
				pause();
				right.drop();
				left.drop();
			}
		} catch (InterruptedException e) {
			System.out.println(this + " 经由中断退出了");
		}
	}

	@Override
	public String toString() {
		return "哲学家 " + id;
	}
}

/**
 * 筷子
 * 
 * @author Administrator
 *
 */
class Chopstick {
	private boolean taken = false;

	public synchronized void take() throws InterruptedException {
		while (taken) {
			wait();
		}
		taken = true;
	}

	public synchronized void drop() {
		taken = false;
		notifyAll();
	}
}
