package com.liukai.thinkinjava.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HorseRace {

	static final int FINISH_LINE = 75;
	private List<Horse> horses = new ArrayList<>();
	private ExecutorService es = Executors.newCachedThreadPool();
	private CyclicBarrier barrier;

	public HorseRace(int nHorses, final int pause) {
		Runnable runnable = new Runnable() {

			/**
			 * 作用：输出各个马的成绩，并中断线程结束比赛
			 */
			public void run() {
				StringBuilder s = new StringBuilder();
				for (int i = 0; i < FINISH_LINE; i++) {
					s.append("=");
				}
				System.out.println(s);

				for (Horse horse : horses) {
					System.out.println(horse.tracks());
				}
				for (Horse horse : horses) {
					if (horse.getStrides() >= FINISH_LINE) {
						System.out.println(horse + " won!");
						es.shutdownNow();
						return;
					}
				}
				try {
					TimeUnit.MILLISECONDS.sleep(pause);
				} catch (InterruptedException e) {
					System.out.println("barrier-action sleep interrupted");
				}
			}

		};

		barrier = new CyclicBarrier(nHorses,runnable);

		for (int i = 0; i < nHorses; i++) {
			Horse horse = new Horse(barrier);
			horses.add(horse);
			es.execute(horse);
		}

	}

	public static void main(String[] args) {
		int nHorses = 7;
		int pause = 200;

		new HorseRace(nHorses, pause);
	}
}

class Horse implements Runnable {

	private static int counter;
	private final int id = counter++;
	private int strides;
	private static Random random = new Random();
	private CyclicBarrier barrier;

	public Horse(CyclicBarrier barrier) {
		this.barrier = barrier;
	}

	public synchronized int getStrides() {
		return strides;
	}

	public void run() {
		try {
			while (!Thread.interrupted()) {
				synchronized (this) {
					strides += random.nextInt(3);
				}
				// System.out.println("等待" + tracks());
				// TimeUnit.MILLISECONDS.sleep(300);
				barrier.await();
			}
		} catch (InterruptedException | BrokenBarrierException e) {
		}
	}

	public String toString() {
		return "Horse " + id;
	}

	public String tracks() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < getStrides(); i++) {
			sb.append("*");
		}
		sb.append(id);
		return sb.toString();
	}

}
