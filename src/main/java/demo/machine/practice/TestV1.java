package demo.machine.practice;


import demo.machine.executor.SimpleTaskExecutor;
import demo.machine.task.Flow;
import demo.machine.task.StatemachineTask;

import static demo.machine.practice.DefaultStatemachineState.*;

/**
 * for testing the statemachine:
 * V1:
 * 只满足一个Task的多阶段运行即可。
 */
public class TestV1 {

    /**
     * 每个阶段加1，加到5结束，每个阶段sleep 1s.
     */
    static class StateMachineTaskV1 extends StatemachineTask<DefaultStatemachineState> {

        public StateMachineTaskV1() {
            this(DefaultStatemachineState.STAGE_INITIAL);
        }

        public StateMachineTaskV1(DefaultStatemachineState stateV1) {
            super(stateV1, null);
        }

        private int value = 0;

        private void action() {
            value++;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(value);
        }

        @Override
        protected Flow executeFromState(DefaultStatemachineState state) {
            switch (state) {
                case STAGE_INITIAL:
                    action();
                    setNextState(STAGE_1);
                    break;
                case STAGE_1:
                    action();
                    setNextState(STAGE_2);
                    break;
                case STAGE_2:
                    action();
                    setNextState(STAGE_3);
                    break;
                case STAGE_3:
                    action();
                    setNextState(STAGE_DONE);
                    break;
                case STAGE_DONE:
                    return Flow.HAS_NO_FLOW;
                default:
                    ;
            }
            return Flow.HAS_MORE_FLOW;
        }

        @Override
        public void rollback() {

        }

        @Override
        public boolean canRollback() {
            return false;
        }

        @Override
        public DefaultStatemachineState getState(int id) {
            return DefaultStatemachineState.forCode(id);
        }

        @Override
        protected Flow rollbackFromState(DefaultStatemachineState state) {
            return null;
        }

        @Override
        public int getStateCode(DefaultStatemachineState state) {
            return state.getCode();
        }
    }

    public static void main(String[] args) {
        SimpleTaskExecutor executor = new SimpleTaskExecutor();
        executor.submit(new StateMachineTaskV1());

        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        executor.submit(new StateMachineTaskV1());
        executor.shutDownGracefully();
    }
}
