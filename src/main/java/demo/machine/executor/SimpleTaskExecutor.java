package demo.machine.executor;

import demo.machine.task.Task;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

public class SimpleTaskExecutor implements TaskExecutor {

    Logger logger = Logger.getLogger(SimpleTaskExecutor.class.getName());

    private final BlockingDeque<Task> tasks = new LinkedBlockingDeque<>();

    private volatile boolean running = true;

    private volatile boolean shutdownGracefully = false;

    WorkThread[] workers = null;

    AtomicLong taskId = new AtomicLong(0);

    public SimpleTaskExecutor() {
        this(Runtime.getRuntime().availableProcessors());
    }

    public SimpleTaskExecutor(int workerNumber) {
        workers = new WorkThread[workerNumber];
        for (int i = 0; i < workers.length; i++) {
            workers[i] = new WorkThread("WorkThread-" + i);
            workers[i].start();
        }
    }

    @Override
    public long submit(Task task) {
        tasks.add(task);
        if (task.getID() == 0) {
            task.setID(taskId.incrementAndGet());
            task.setExecutor(this);
        }
        return task.getID();
    }

    public boolean isRunning() {
        return running || shouldRun();
    }

    public boolean shouldRun() {
        return shutdownGracefully && tasks.size() > 0;
    }

    public void shutDownGracefully() {
        shutdownGracefully = true;
        running = false;
    }

    public void shutdownNow() {
        running = false;
    }

    class WorkThread extends Thread {

        String name;

        public WorkThread(String name) {
            this.name = name;
        }

        public void run() {

            Thread.currentThread().setName(name);

            while (isRunning()) {

                Task task = null;
                try {
                    task = tasks.poll(100, TimeUnit.MILLISECONDS);
                    if (task == null) {
                        continue;
                    }
                    try {
                        if (task.isRollBack()) {
                            task.rollback();
                        } else {
                            task.execute();
                        }

                        if (task.isRollBack()) {
                            makeFinished(task);
                        }

                        if (task.isFailed()) {
                            if (task.canRollback()) {
                                task.setRollBack();
                                tasks.add(task);
                            }
                        }

                        if (task.isDone()) {
                            makeFinished(task);
                        }

                    } catch (Exception e) {
                        // TODO 处理异常。
                        e.printStackTrace();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println(name + " stopped");
        }
    }

    /**
     * 设置这个task为完成状态，并且告知用户;
     * 1、成功
     * 2、失败
     * 2.1 执行失败
     * 2.2 回滚失败
     *
     * @param task
     */
    private void makeFinished(Task task) {
        boolean isSuccessful = true;
        String failedMessage = null;
        if (task.isFailed()) {
            isSuccessful = false;
            failedMessage = task.getFailedException().getMessage();
        }

        if (isSuccessful)
            logger.info("task = " + task.getID() + (isSuccessful ? " done." : (" failed, cause = " + failedMessage)));
        else
            logger.info("task = " + task.getID() + (isSuccessful ? " done." : (" failed, cause = " + failedMessage)));

        // TODO notify the submit caller;

    }

}
