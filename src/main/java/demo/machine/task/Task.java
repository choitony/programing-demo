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
        FINISHED
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

    public boolean isRollbacking() {
        return lifecycle == Lifecycle.RUNNING && isRollBack();
    }

    public boolean isDone() {
        return lifecycle == Lifecycle.FINISHED;
    }

    public boolean isSuspended() {
        return lifecycle == Lifecycle.SUSPENDED;
    }

    public boolean isFailed() {
        return failedException != null;
    }

    public boolean isRollbackFailed() {
        return rollbackException != null;
    }

    public boolean isRollBack() {
        return rollback;
    }

    // TODO ?????????????????????
    public boolean isRollbackDONE() {
        return rollback && lifecycle == Lifecycle.FINISHED && !isRollbackFailed();
    }

    public abstract boolean canRollback();

    // ????????????????????????StatemachineTask,???????????????
    public void addChildrenTask(Task task) {
        subTasks.add(task);
    }

    /**
     * ?????????????????????
     *
     * @return ?????????????????????????????????????????????????????????
     */
    public boolean resume() {
        if (isSuspended()) {
            lifecycle = Lifecycle.RUNNING;
        }
        for (Task son : subTasks) {
            if (son.isFailed()) {
                setFailed(new ExecutionException("son task" + son.getID() + " execute failed.", son.failedException));
                logger.warning("task: " + getID() + " resumed failed, cause son task: " + son.getID() + " executed failed with reason = " + son.getFailedException().getMessage());
                return false;
            }
        }
        logger.info("task: " + getID() + " resumed successfully.");
        subTasks = new ArrayList<>();
        return true;
    }

    public boolean rollbackResume() {
        if (isSuspended()) {
            lifecycle = Lifecycle.RUNNING;
        }
        for (Task son : subTasks) {
            if (son.isRollbackFailed()) {
                setRollbackException(new ExecutionException("son task" + son.getID() + " execute failed.", son.failedException));
                logger.warning("task: " + getID() + " rollback resumed failed, cause son task: " + son.getID() + " rollback failed with reason = " + son.getFailedException().getMessage());
                return false;
            }
        }
        logger.info("task : " + getID() + " rollback resume successfully");
        subTasks = new ArrayList<>();
        return true;
    }

    public void suspendAndSubmitChildren() {
        if (isRunning()) {
            lifecycle = Lifecycle.SUSPENDED;
        } else if (isRollbacking()) {
            lifecycle = Lifecycle.SUSPENDED;
        }
        {
            // TODO ???????????????????????????
        }
        sonCountToDone.set(subTasks.size());

        String subIDs = "[";
        for (Task task : subTasks) {
            long subID = getExecutor().submit(task);
            subIDs = subIDs + subID + ",";
        }

        logger.info("task: " + getID() + " suspended, and submit sub task: " + subIDs.substring(0, subIDs.length() - 1) + "]");
    }

    /**
     * ????????????????????????????????????
     * ?????????????????????????????????
     * ?????????????????????????????????????????????????????????????????????????????????
     * ????????????????????????????????????????????????????????????
     * ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     * ?????????????????????????????????????????????????????????????????????
     */
    public void complete() {
        if (lifecycle == Lifecycle.RUNNING) {
            lifecycle = Lifecycle.FINISHED;
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
        lifecycle = Lifecycle.RUNNABLE;
        rollback = true;
    }

    public Exception getFailedException() {
        return failedException;
    }

    public void setFailed(Exception e) {
        failedException = e;
        logger.warning("task : " + getID() + " failed, cause = " + e.getMessage());
    }

    public Exception getRollbackException() {
        return rollbackException;
    }

    public void setRollbackException(Exception rollbackException) {
        this.rollbackException = rollbackException;
    }
}
