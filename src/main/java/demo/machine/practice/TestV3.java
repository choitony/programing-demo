package demo.machine.practice;

import demo.machine.executor.SimpleTaskExecutor;
import demo.machine.task.Flow;
import demo.machine.task.StatemachineTask;

/**
 * 实现如下回滚功能：
 * 1、设计一个定义了回滚逻辑的RooBackTask：每个阶段对计数器加一，一共加三次；回滚操作为每次减一，抵消掉操作。
 */
public class TestV3 {


    static class RollBackTask extends StatemachineTask<DefaultStatemachineState> {

        int value = 2;

        public RollBackTask() {
            this(DefaultStatemachineState.STAGE_INITIAL);
        }

        public RollBackTask(DefaultStatemachineState state) {
            super(state, null);
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
        protected Flow executeFromState(DefaultStatemachineState DefaultStatemachineState) {

            switch (DefaultStatemachineState) {
                case STAGE_INITIAL:
                    setNextState(DefaultStatemachineState.STAGE_1);
                    break;
                case STAGE_1:
                    value++;
                    setNextState(DefaultStatemachineState.STAGE_2);
                    break;
                case STAGE_2:
                    value *= 2;
                    setNextState(DefaultStatemachineState.STAGE_3);
                    break;
                case STAGE_3:
                    if (value == 6)
                       ;// throw new IllegalAccessException("task = " + getID() + " ADD_2 failed, please rollback, current value = " + value);
                    value += 2;
                    setNextState(DefaultStatemachineState.STAGE_DONE);
                    break;
                case STAGE_DONE:
                    complete();
                    System.out.println("task done with value = " + value);
                    return Flow.HAS_NO_FLOW;
                default:
                    ;//throw new IllegalAccessException("invalid state" + DefaultStatemachineState);
            }

            return Flow.HAS_MORE_FLOW;
        }

        @Override
        public Flow rollbackFromState(DefaultStatemachineState DefaultStatemachineState) throws Exception {

            switch (DefaultStatemachineState) {
                case STAGE_INITIAL:
                    System.out.println("roll back done with value = " + value);
                    return Flow.HAS_NO_FLOW;
                case STAGE_1:
                    setNextState(DefaultStatemachineState.STAGE_INITIAL);
                    break;
                case STAGE_2:
                    value--;
                    setNextState(DefaultStatemachineState.STAGE_1);
                    break;
                case STAGE_3:
                    value /= 2;
                    setNextState(DefaultStatemachineState.STAGE_2);
                    break;
                case STAGE_DONE:
                    setNextState(DefaultStatemachineState.STAGE_3);
                default:
                    throw new IllegalAccessException("invalid state" + DefaultStatemachineState);
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
