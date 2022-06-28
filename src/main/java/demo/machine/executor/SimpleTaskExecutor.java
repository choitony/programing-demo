package demo.machine.executor;

import demo.machine.task.Task;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class SimpleTaskExecutor implements TaskExecutor {

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
        task.setID(taskId.incrementAndGet());
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
                    if(task == null){
                        continue;
                    }
                    try {
                        do {
                            Task[] subTasks = task.execute();

                            // 如果返回自己,表示这一阶段完成，需要继续执行。
                            // 如果返回空，表示整个任务完成。需要看下其是否有父任务，如果有需要判断是否需要唤醒父任务。
                            // 如果返回其他，表示有子任务，执行子任务，然后挂起当前任务。
                            if (subTasks.length == 0) {
                                task.complete();
                                if(task.isRollBack()){
                                    task.getParent().setRollBack();
                                    task.getParent().rollbackAllChild();
                                }
                                if (task.hasParent() && task.getParent().canRun()) {
                                    tasks.add(task.getParent());
                                }
                            } else if (subTasks.length == 1 && subTasks[0] == task) {
                                // continue in next loop;
                            } else {
                                for (Task subtask : subTasks) {
                                    tasks.add(subtask);
                                }
                            }
                        } while (!task.isDone() && !task.isSuspended());
                    } catch (Exception e) {
                        System.out.println("roll back task = " + task.getID() + " with reason = [ " + e.getMessage() + " ]");
                        // 任务只关注自己执行的情况，不需要关注回滚的触发，回滚的触发由引擎捕获任务的执行异常来处理。
                        if (task.isFailed()) {
                            task.setRollBack();
                            tasks.add(task);
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println(name + " stopped");
        }
    }


}
