package demo.machine.executor;

import demo.machine.task.Task;
import org.junit.Test;

public class TestTaskMachineExecutorLifecycle {

    class SleepTask extends Task {

        public SleepTask(){
            this(null);
        }

        public SleepTask(Task parent) {
            super(parent);
        }

        @Override
        public Task[] execute() throws Exception {
            this.lifecycle = Lifecycle.RUNNING;
            Thread.sleep(1000l);
            System.out.println("Task - " + getID() + " sleep task done");
            return new Task[0];
        }

        @Override
        public void rollback() {

        }
    }

    @Test
    public void testSleepTask() {
        SimpleTaskExecutor executor = new SimpleTaskExecutor();

        for (int i = 0; i < 2; i++) {
            executor.submit(new SleepTask());
        }
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        executor.shutdownNow();
    }

    @Test
    public void testShutdownNow() {
        SimpleTaskExecutor executor = new SimpleTaskExecutor();

        for (int i = 0; i < 10; i++) {
            executor.submit(new SleepTask());
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        executor.shutdownNow();

        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testShutdownGracefully() {
        SimpleTaskExecutor executor = new SimpleTaskExecutor();

        for (int i = 0; i < 10; i++) {
            executor.submit(new SleepTask());
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        executor.shutDownGracefully();

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
