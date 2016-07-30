package com.liukai.thinkinjava.concurrent;

import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 银行出纳员仿真系统
 * 在本例中，每个银行顾客要求一定数量的服务时间，这是出纳员必须花费在顾客身上，以服务需求的时间单位的数量。服务时间的数量对每个顾客来说是不同的，并且是随机确定的
 * Created by Administrator on 2016/7/30 0030.
 */
public class BankTellerSimulation {

    private static final int MAX_LINE_SIZE = 50;
    private static final int ADJUSTMENT_PERIOD = 1000;

    public static void main(String[] args) throws InterruptedException {
        ExecutorService es = Executors.newCachedThreadPool();
        CustomerLine customers = new CustomerLine(MAX_LINE_SIZE);

        //执行生成顾客任务
        es.execute(new CustomerGenerator(customers));
        //执行出纳员管理器任务
        es.execute(new TellerManager(ADJUSTMENT_PERIOD, es, customers));

        TimeUnit.SECONDS.sleep(5);
        es.shutdownNow();

    }
}

/**
 * 出纳员任务管理器，主要任务是调整出纳员的数量
 */
class TellerManager implements Runnable {

    private static Random random = new Random(47);
    private ExecutorService es;
    private CustomerLine customers;//顾客队列
    private PriorityQueue<Teller> workingTellers = new PriorityQueue<>();//优先级队列，工作中的出纳员
    private Queue<Teller> tellersDoingOtherThings = new LinkedList<>();//做其他事情的出纳员
    private int adjustmentPeriod;//调整周期

    public TellerManager(int adjustmentPeriod, ExecutorService es, CustomerLine customers) {
        this.adjustmentPeriod = adjustmentPeriod;
        this.es = es;
        this.customers = customers;

        Teller teller = new Teller(customers);
        workingTellers.add(teller);
    }

    /**
     * 调整出纳员数量
     */
    private void adjustmentTellerNumber() {
        //如果顾客队列很长，就添加一个出纳员
        if (customers.size() / workingTellers.size() > 2) {

            //如果做其他事情的出纳员队列中有出纳员
            if (tellersDoingOtherThings.size() > 0) {
                Teller teller = tellersDoingOtherThings.remove();
                teller.serveCustomerLine();
                workingTellers.add(teller);
                return;
            }
            //否则创建一个出纳员
            Teller teller = new Teller(customers);
            es.execute(teller);
            workingTellers.add(teller);
            return;
        }

        //如果顾客队列很短，就移除一个出纳员
        if (workingTellers.size() > 1 && customers.size() / workingTellers.size() < 2) {
            reassignOneTeller();
        }

        //如果没有顾客排队，我们仅仅只需要一个出纳员
        if (customers.size() == 0) {
            while (workingTellers.size() > 1) {
                reassignOneTeller();
            }
        }
    }

    /**
     * 重新分配一个出纳员
     */
    private void reassignOneTeller() {
        Teller teller = workingTellers.poll();
        teller.doSomethingElse();
        tellersDoingOtherThings.add(teller);
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                TimeUnit.MILLISECONDS.sleep(adjustmentPeriod);
                adjustmentTellerNumber();
                System.out.print(customers + " { ");
                for (Teller teller : workingTellers) {
                    System.out.print(teller + " ");
                }
                System.out.println("}");
            }
        } catch (InterruptedException e) {
            System.out.println("出纳员管理器任务被中断");
        }
        System.out.println("出纳员管理器任务终止");
    }
}

/**
 * 出纳员任务
 * 出纳员一直服务顾客，直到出纳员做其他事情
 */
class Teller implements Runnable, Comparable<Teller> {

    private static int counter = 0;
    private final int id = counter++;
    private int customersServed = 0;
    private CustomerLine customers;
    private boolean servingCustomerLine = true;

    public Teller(CustomerLine customers) {
        this.customers = customers;
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                Customer customer = customers.take();
                TimeUnit.MILLISECONDS.sleep(customer.getServiceTime());
                synchronized (this) {
                    customersServed++;
                    while (!servingCustomerLine) {
                        wait();
                    }
                }
            }
        } catch (InterruptedException e) {
            System.out.println("出纳员任务被中断");
        }
        System.out.println("出纳员任务终止");
    }

    @Override
    public synchronized int compareTo(Teller o) {
        return customersServed > o.customersServed ? 1 : (customersServed < o.customersServed ? -1 : 0);
    }

    /**
     * 做其他的事情
     */
    public synchronized void doSomethingElse() {
        customersServed = 0;
        servingCustomerLine = false;
    }

    /**
     * 服务顾客
     */
    public synchronized void serveCustomerLine() {
        assert !servingCustomerLine : "already serving: " + this;
        servingCustomerLine = true;
        notifyAll();
    }

    public String shortString() {
        return "T: " + id;
    }

    @Override
    public String toString() {
        return "Teller: " + id;
    }
}

/**
 * 生成顾客任务
 */
class CustomerGenerator implements Runnable {

    private CustomerLine customers;
    private static Random random = new Random(47);

    public CustomerGenerator(CustomerLine customers) {
        this.customers = customers;
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                TimeUnit.MILLISECONDS.sleep(random.nextInt(300));
                customers.put(new Customer(random.nextInt(1000)));
            }
        } catch (InterruptedException e) {
            System.out.println("生成顾客任务被中断");
        }
        System.out.println("生成顾客任务终止");
    }
}

/**
 * 顾客队列
 */
class CustomerLine extends ArrayBlockingQueue<Customer> {

    public CustomerLine(int maxLineSize) {
        super(maxLineSize);
    }

    @Override
    public String toString() {
        if (this.size() == 0) {
            return "[empty]";
        }
        StringBuilder result = new StringBuilder();
        for (Customer customer : this) {
            result.append(customer);
        }
        return result.toString();
    }
}

/**
 * 顾客
 * 只读的对象，不需要同步
 */
class Customer {

    private final int serviceTime;//服务时间

    public Customer(int serviceTime) {
        this.serviceTime = serviceTime;
    }

    public int getServiceTime() {
        return serviceTime;
    }

    @Override
    public String toString() {
        return "[顾客需要服务的时间：" + serviceTime + "ms]";
    }
}


