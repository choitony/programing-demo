package demo.machine.task;

import java.util.Arrays;
import java.util.logging.Logger;

public abstract class StatemachineTask<TState> extends Task {

    Logger logger = Logger.getLogger(StatemachineTask.class.getName());

    private int[] statemachineStates;
    private int currentStateIndex;

    public StatemachineTask(TState initialState, Task parent) {
        super(parent);
        statemachineStates = new int[1];
        currentStateIndex = 0;
        statemachineStates[0] = getStateCode(initialState);
        this.parent = parent;
    }

    @Override
    public void execute() {

        if (isSuspended()) {
            boolean ret = resume();
            if (!ret) {
                return;
            }
        }

        if (isRunnable()) {
            lifecycle = Lifecycle.RUNNING;
        }

        statemachineExecute();
        complete();
    }

    public void statemachineExecute() {
        do {
            TState state = getCurrentState();
            Flow flow = executeFromState(state);

            if (isFailed()) {
                return;
            }

            if (hasChildren()) {
                suspendAndSubmitChildren();
            }

            if (!hasMoreFlow(flow)) {
                return;
            }
        } while (!(isDone() || isFailed() || isSuspended()));
    }

    public boolean hasChildren() {
        return subTasks != null && subTasks.size() > 0;
    }

    public void rollback() {
        if (isSuspended()) {
            boolean ret = rollbackResume();
            if (!ret) {
                return;
            }
        }

        if(isRunnable()){
            lifecycle = Lifecycle.RUNNING;
        }

        statemachineRollback();

        complete();
    }

    void statemachineRollback() {
        do{
            TState state = getCurrentState();
            Flow flow = rollbackFromState(state);
            if(isRollbackFailed()){
                return;
            }
            if(!hasMoreFlow(flow)){
                lifecycle = Lifecycle.FINISHED;
                return;
            }
            if(hasChildren()){
                suspendAndSubmitChildren();
            }
        }while(!(isRollbackFailed() || isRollbackDONE() || isSuspended()));
    }

    public TState getCurrentState() {
        return getState(statemachineStates[currentStateIndex]);
    }

    public abstract TState getState(int id);

    public abstract int getStateCode(TState state);

    protected abstract Flow executeFromState(TState state);

    protected abstract Flow rollbackFromState(TState state);

    public boolean hasMoreFlow(Flow flow) {
        return flow == Flow.HAS_MORE_FLOW;
    }

    public void setNextState(TState tState) {
        statemachineStates = Arrays.copyOf(statemachineStates, statemachineStates.length + 1);
        statemachineStates[statemachineStates.length - 1] = getStateCode(tState);
        currentStateIndex++;
    }

    public void rollbackState() {
        currentStateIndex--;
        statemachineStates = Arrays.copyOf(statemachineStates, statemachineStates.length - 1);
    }
}
