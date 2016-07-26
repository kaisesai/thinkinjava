package com.liukai.thinkinjava.concurrent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 优先级阻塞队列例子
 * 
 * @author Administrator
 *
 */
public class PriorityBlockingQueueDemo {

	public static void main(String[] args) {
		ExecutorService es = Executors.newCachedThreadPool();
		PriorityBlockingQueue<Runnable> queue = new PriorityBlockingQueue<>();
		es.execute(new PrioritizedTaskProducer(queue, es));// 开启生产者线程
		es.execute(new PrioritizedTaskConsumer(queue));// 开启消费者线程
	}

}

/**
 * 优先级任务提供者
 * 
 * @author Administrator
 *
 */
class PrioritizedTaskProducer implements Runnable {

	private Random random = new Random(47);
	private Queue<Runnable> queue;
	private ExecutorService es;

	public PrioritizedTaskProducer(Queue<Runnable> queue, ExecutorService es) {
		this.queue = queue;
		this.es = es;
	}

	@Override
	public void run() {
		// 存放20个随机的优先级任务
		for (int i = 0; i < 20; i++) {
			queue.add(new PrioritizedTask(random.nextInt(10)));
			Thread.yield();
		}
		try {
			// 存放10个优先级最高的优先级任务
			for (int i = 0; i < 10; i++) {
				TimeUnit.MILLISECONDS.sleep(250);
				queue.add(new PrioritizedTask(10));
			}
			// 存放10个优先级由小到大优先级任务
			for (int i = 0; i < 10; i++) {
				queue.add(new PrioritizedTask(i));
			}
			// 存放优先级最低的终结线程的任务
			queue.add(new PrioritizedTask.EndSentinel(es));
		} catch (InterruptedException e) {
		}
		System.out.println("任务都已经存放完毕...");

	}
}

/**
 * 优先级任务消费者——消费所有的优先级任务
 * 
 * @author Administrator
 *
 */
class PrioritizedTaskConsumer implements Runnable {

	private PriorityBlockingQueue<Runnable> queue;

	public PrioritizedTaskConsumer(PriorityBlockingQueue<Runnable> queue) {
		this.queue = queue;
	}

	@Override
	public void run() {
		while (!Thread.interrupted()) {
			try {
				queue.take().run();
			} catch (InterruptedException e) {
				System.out.println(e.getMessage());
			}
		}
		System.out.println("所有的任务都已经消费完毕...");
	}
}

/**
 * 优先级任务
 * 
 * @author Administrator
 *
 */
class PrioritizedTask implements Runnable, Comparable<PrioritizedTask> {

	private Random random = new Random(47);
	private static int count = 0;
	private final int id = ++count;
	private final int priority;// 优先级
	protected static List<PrioritizedTask> sequence = new ArrayList<>();// 记录保存的顺序

	public PrioritizedTask(int priority) {
		this.priority = priority;
		sequence.add(this);
	}

	@Override
	public int compareTo(PrioritizedTask task) {
		return priority > task.priority ? -1 : (priority < task.priority ? 1 : 0);
	}

	@Override
	public void run() {
		try {
			TimeUnit.MILLISECONDS.sleep(random.nextInt(250));
		} catch (InterruptedException e) {
		}
		System.out.println("执行任务：" + this);
	}

	@Override
	public String toString() {
		return String.format("[%1$Ty年%1$Tm月%1$Td日 %1$TH:%1$TM:%1$TS]", new Date()) + String.format("[%1$-3d]", priority)
				+ " Task: " + id;
	}

	public String summary() {
		return "(" + id + ":" + priority + ")";
	}

	/**
	 * 终结线程哨兵 ——终止所有线程，并打印优先级任务的顺序
	 * 
	 * @author Administrator
	 *
	 */
	public static class EndSentinel extends PrioritizedTask {

		private ExecutorService es;

		public EndSentinel(ExecutorService es) {
			super(-1);
			this.es = es;
		}

		@Override
		public void run() {
			for (PrioritizedTask pt : sequence) {
				System.out.println("按照任务的插入顺序输出：" + pt.summary());
			}
			System.out.println("sequence的数量：" + sequence.size());
			System.out.println(this + " 执行了shutdownNow()方法");
			es.shutdownNow();
		}

	}

}
