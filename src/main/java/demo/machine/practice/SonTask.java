package demo.machine.practice;

import demo.machine.task.Flow;
import demo.machine.task.StatemachineTask;

/**
 * 测试用的子任务
 */
public class SonTask extends StatemachineTask<DefaultStatemachineState> {

    String name;

    public SonTask(String name, StatemachineTask parent) {
        this(DefaultStatemachineState.STAGE_INITIAL, parent);
        this.name = name;
    }

    public SonTask(DefaultStatemachineState initialState, StatemachineTask parent) {
        super(initialState, parent);
    }

    @Override
    public DefaultStatemachineState getState(int id) {
        return DefaultStatemachineState.forCode(id);
    }

    @Override
    public int getStateCode(DefaultStatemachineState defaultStatemachineState) {
        return defaultStatemachineState.getCode();
    }

    @Override
    protected Flow executeFromState(DefaultStatemachineState son_stage) {
        switch (son_stage) {
            case STAGE_INITIAL:
                System.out.println("son task initial");
                setNextState(DefaultStatemachineState.STAGE_DONE);
                break;
            case STAGE_DONE:
                System.out.println("son task done");
                return Flow.HAS_NO_FLOW;
            default:
                ;//throw new Exception("invalid state");
        }
        return Flow.HAS_MORE_FLOW;
    }

    @Override
    protected Flow rollbackFromState(DefaultStatemachineState defaultStatemachineState) throws Exception {
        return null;
    }


}

enum SON_STAGE {
    INITIAL,
    DONE;
}
