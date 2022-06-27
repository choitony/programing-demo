package demo.machine.executor;

import demo.machine.task.StatemachineTask;
import demo.machine.task.Task;

public interface TaskExecutor {

    /**
     * submit a task
     * @param task
     * @return
     */
    public long submit(Task task);
}
