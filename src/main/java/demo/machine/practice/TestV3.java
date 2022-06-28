package demo.machine.practice;

import demo.machine.executor.SimpleTaskExecutor;
import demo.machine.task.Flow;
import demo.machine.task.StatemachineTask;

/**
 * 实现如下回滚功能：
 * 1、设计一个定义了回滚逻辑的RooBackTask：每个阶段对计数器加一，一共加三次；回滚操作为每次减一，抵消掉操作。
 */
public class TestV3 {

    enum State_V3 {
        INITIAL,
        ADD_1,
        MULTI_2,
        ADD_2,
        DONE;
    }


    static class RollBackTask extends StatemachineTask<State_V3> {

        int value = 2;

        public RollBackTask() {
            this(State_V3.INITIAL);
        }

        public RollBackTask(State_V3 state) {
            super(state, null);
        }

        @Override
        protected Flow executeFromState(State_V3 state_v3) throws Exception {

            switch (state_v3) {
                case INITIAL:
                    setNextState(State_V3.ADD_1);
                    break;
                case ADD_1:
                    value++;
                    setNextState(State_V3.MULTI_2);
                    break;
                case MULTI_2:
                    value *= 2;
                    setNextState(State_V3.ADD_2);
                    break;
                case ADD_2:
                    if (value == 6)
                        throw new Exception("task = " + getID() + " ADD_2 failed, please rollback, current value = " + value);
                    value += 2;
                    setNextState(State_V3.DONE);
                    break;
                case DONE:
                    complete();
                    System.out.println("task done with value = " + value);
                    return Flow.HAS_NO_FLOW;
                default:
                    throw new IllegalAccessException("invalid state" + state_v3);
            }

            return Flow.HAS_MORE_FLOW;
        }

        @Override
        public Flow rollbackFromState(State_V3 state_v3) throws Exception {

            switch (state_v3) {
                case INITIAL:
                    System.out.println("roll back done with value = " + value);
                    return Flow.HAS_NO_FLOW;
                case ADD_1:
                    setNextState(State_V3.INITIAL);
                    break;
                case MULTI_2:
                    value--;
                    setNextState(State_V3.ADD_1);
                    break;
                case ADD_2:
                    value /= 2;
                    setNextState(State_V3.MULTI_2);
                    break;
                case DONE:
                    setNextState(State_V3.ADD_2);
                default:
                    throw new IllegalAccessException("invalid state" + state_v3);
            }
            return Flow.HAS_MORE_FLOW;
        }
    }

    public static void main(String[] args) {
        SimpleTaskExecutor ste = new SimpleTaskExecutor();
        ste.submit(new RollBackTask());
        ste.shutDownGracefully();
    }

}
