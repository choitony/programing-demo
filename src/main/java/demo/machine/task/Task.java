package demo.machine.task;

import demo.machine.executor.SimpleTaskExecutor;
import demo.machine.executor.TaskExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * the class define the interface of a task that can be executed by TaskExecutor.
 * <p>
 * by TaskExecutor#submit(Task) can execute a Task.
 */
public abstract class Task {

    Logger logger = Logger.getLogger(Task.class.getName());

    protected enum Lifecycle {
        RUNNABLE,
        RUNNING,
        SUSPENDED,
        DONE,
        FAILED
    }

    public Task(Task parent) {
        this.parent = parent;
    }

    TaskExecutor executor = null;

    volatile boolean rollback = false;

    protected Lifecycle lifecycle = Lifecycle.RUNNABLE;

    long ID;

    private Exception failedException;

    private Exception rollbackException;

    /**
     * @return a set of subTasks to run or ourselves if there is no more work to do or null if the task is done.
     */
    public abstract void execute() throws Exception;

    public void setExecutor(TaskExecutor executor) {
        this.executor = executor;
    }

    public TaskExecutor getExecutor() {
        return this.executor;
    }

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

    // TODO 这个表述不准确
    public boolean isRollbackFinished() {
        return rollback && lifecycle == Lifecycle.RUNNABLE;
    }

    public abstract boolean canRollback();

    // 这里没必要限制为StatemachineTask,后续在设计
    public void addChildrenTask(Task task) {
        subTasks.add(task);
    }

    /**
     * 恢复挂起的任务
     *
     * @return 如果子任务中有失败的，当前任务直接失败
     */
    public boolean resume() {
        if (isSuspended()) {
            lifecycle = Lifecycle.RUNNING;
        }
        for (Task son : subTasks) {
            if (son.isFailed()) {
                setFailedException(new ExecutionException("son task" + son.getID() + " execute failed.", son.failedException));
                logger.warning("task: " + getID() + " resumed failed, cause son task: " + son.getID() + " executed failed with reason = " + son.getFailedException().getMessage());
                return false;
            }
        }
        logger.warning("task: " + getID() + " resumed successfully.");
        subTasks = new ArrayList<>();
        return true;
    }

    public void suspendAndSubmitChildren() {
        if (isRunning()) {
            lifecycle = Lifecycle.SUSPENDED;
            sonCountToDone.set(subTasks.size());
        }

        String subIDs = "[";
        for (Task task : subTasks) {
            long subID = getExecutor().submit(task);
            subIDs = subIDs + subID + ",";
        }

        logger.info("task: " + getID() + " suspended, and submit sub task: " + subIDs.substring(0, subIDs.length() - 1) + "]");
    }

    /**
     * 处理任务完成、失败、挂起
     * 成功，做成功状态转变。
     * 失败，交由上层处理。失败的状态在出异常的时候已经设置。
     * 挂起，挂起的状态设置也在挂起的时候设置。
     * 另外，无论成功还是失败，都要对父任务进行计数器减一。帮助父任务恢复调度！！！
     * 最后，如果可以父任务达到运行条件，调度父任务。
     */
    public void complete() {
        if (lifecycle == Lifecycle.RUNNING) {
            lifecycle = Lifecycle.DONE;
        }

        if (hasParent() && !isSuspended()) {
            getParent().sonCountToDone.decrementAndGet();
            if (getParent().isSuspended() && getParent().canRun()) {
                getExecutor().submit(getParent());
            }
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

    public Exception getFailedException() {
        return failedException;
    }

    public void setFailedException(Exception failedException) {
        this.failedException = failedException;
    }

    public Exception getRollbackException() {
        return rollbackException;
    }

    public void setRollbackException(Exception rollbackException) {
        this.rollbackException = rollbackException;
    }
}
