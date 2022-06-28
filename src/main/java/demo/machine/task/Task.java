package demo.machine.task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * the class define the interface of a task that can be executed by TaskExecutor.
 * <p>
 * by TaskExecutor#submit(Task) can execute a Task.
 */
public abstract class Task {

    protected enum Lifecycle {
        RUNNABLE,
        RUNNING,
        SUSPENDED,
        DONE,
        FAILED
    }

    public Task(Task parent){
        this.parent = parent;
    }

    volatile boolean rollback = false;

    protected Lifecycle lifecycle = Lifecycle.RUNNABLE;

    long ID;

    /**
     * @return a set of subTasks to run or ourselves if there is no more work to do or null if the task is done.
     */
    public abstract Task[] execute() throws Exception;

    public Task parent;
    private AtomicInteger sonCountToDone = new AtomicInteger(0);
    protected List<Task> subTasks = new ArrayList<>();

    public void setID(long id) {
        ID = id;
    }

    public long getID() {
        return ID;
    }

    /**
     * The code to undo what was done by the execute() code.
     * It is called when the task or one of the sub-tasks failed or an abort is requested.
     * It should cleanup all the resources created by the execute() call. The implementation must
     * by idempotant since rollback()
     * may be called multiple times in case of machine failure in the middle of the execution.
     */
    public abstract void rollback();

    public boolean isRunnable() {
        return lifecycle == Lifecycle.RUNNABLE;
    }

    public boolean isRunning() {
        return lifecycle == Lifecycle.RUNNING;
    }

    public boolean isDone() {
        return lifecycle == Lifecycle.DONE;
    }

    public boolean isSuspended() {
        return lifecycle == Lifecycle.SUSPENDED;
    }

    public boolean isFailed() {
        return lifecycle == Lifecycle.FAILED;
    }

    public boolean isRollBack() {
        return rollback;
    }

    // 这里没必要限制为StatemachineTask,后续在设计
    public void addChildrenTask(Task task) {
        subTasks.add(task);
    }

    public void resume() {
        if (isSuspended()) {
            lifecycle = Lifecycle.RUNNING;
        }
        subTasks = new ArrayList<>();
    }

    public void suspend(int sonCount) {
        lifecycle = Lifecycle.SUSPENDED;
        sonCountToDone.set(sonCount);
    }

    public void complete() {
        if (lifecycle == Lifecycle.RUNNING || lifecycle == Lifecycle.FAILED) {
            lifecycle = Lifecycle.DONE;
        }

        if (hasParent()) {
            getParent().sonCountToDone.decrementAndGet();
        }
    }

    public boolean canRun() {
        return sonCountToDone.get() == 0;
    }

    public Task getParent() {
        return parent;
    }

    public boolean hasParent() {
        return parent != null;
    }

    public void setRollBack() {
        rollback = true;
    }
}
