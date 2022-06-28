package demo.machine.task;

import java.lang.reflect.Array;
import java.util.Arrays;

public abstract class StatemachineTask<TState> extends Task {

    private int[] statemachineStates;
    private int currentStateIndex;

    public StatemachineTask(TState initialState, Task parent) {
        super(parent);
        statemachineStates = new int[1];
        currentStateIndex = 0;
        statemachineStates[0] = getStateCode(initialState);
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
        return getState(statemachineStates[currentStateIndex]);
    }

    public abstract TState getState(int id);

    public abstract int getStateCode(TState state);

    protected abstract Flow executeFromState(TState state);

    protected abstract Flow rollbackFromState(TState state) throws Exception;

    public boolean hasMoreFlow() {
        return stateFlow == Flow.HAS_MORE_FLOW;
    }

    public void setNextState(TState tState) {
        statemachineStates = Arrays.copyOf(statemachineStates, statemachineStates.length + 1);
        statemachineStates[statemachineStates.length - 1] = getStateCode(tState);
        currentStateIndex++;
    }
}
