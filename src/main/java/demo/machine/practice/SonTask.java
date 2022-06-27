package demo.machine.practice;

import demo.machine.task.Flow;
import demo.machine.task.StatemachineTask;

/**
 * 测试用的子任务
 */
public class SonTask extends StatemachineTask<SON_STAGE> {

    String name;

    public SonTask(String name, StatemachineTask parent) {
        this(SON_STAGE.INITIAL, parent);
        this.name = name;
    }

    public SonTask(SON_STAGE initialState, StatemachineTask parent) {
        super(initialState, parent);
    }

    @Override
    protected Flow executeFromState(SON_STAGE son_stage) throws Exception {
        switch (son_stage) {
            case INITIAL:
                System.out.println("son task initial");
                setNextState(SON_STAGE.DONE);
                break;
            case DONE:
                System.out.println("son task done");
                return Flow.HAS_NO_FLOW;
            default:
                throw new Exception("invalid state");
        }
        return Flow.HAS_MORE_FLOW;
    }
}

enum SON_STAGE {
    INITIAL,
    DONE;
}
