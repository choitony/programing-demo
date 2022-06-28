package demo.machine.task;

public abstract class StatemachineTask<TState> extends Task {

    private TState currentState;


    public StatemachineTask(TState initialState, Task parent) {
        currentState = initialState;
        this.parent = parent;
    }

    Flow stateFlow = Flow.HAS_MORE_FLOW;

    @Override
    public StatemachineTask[] execute() throws Exception {

        if (isRunnable()) {
            lifecycle = Lifecycle.RUNNING;
        }

        if (isSuspended()) {
            resume();
        }

        TState currentState = getCurrentState();
        try {
            stateFlow = null;
            if (isRollBack()) {
                stateFlow = rollbackFromState(currentState);
            } else {
                stateFlow = executeFromState(currentState);
            }
        } catch (Exception e) {
            lifecycle = Lifecycle.FAILED;
            throw e;
        }

        if (!hasMoreFlow()) {
            complete();
            return new StatemachineTask[]{};
        }

        if (subTasks.isEmpty()) {
            return new StatemachineTask[]{this};
        }

        suspend(subTasks.size());

        return subTasks.toArray(new StatemachineTask[subTasks.size()]);
    }

    public void rollback() {
        // see executeFromState
    }

    public TState getCurrentState() {
        return currentState;
    }

    protected abstract Flow executeFromState(TState state) throws Exception;

    protected abstract Flow rollbackFromState(TState state) throws Exception;

    public boolean hasMoreFlow() {
        return stateFlow == Flow.HAS_MORE_FLOW;
    }

    public void setNextState(TState tState) {
        currentState = tState;
    }
}
