package demo.machine.practice;


import demo.machine.executor.SimpleTaskExecutor;
import demo.machine.task.Flow;
import demo.machine.task.StatemachineTask;

/**
 * for testing the statemachine:
 * V1:
 * 只满足一个Task的多阶段运行即可。
 */
public class TestV1 {

    enum StateV1 {
        STAGE_INITIAL,
        STAGE_V1,
        STAGE_V2,
        STAGE_V3,
        STAGE_DONE;
    }

    /**
     * 每个阶段加1，加到5结束，每个阶段sleep 1s.
     */
    static class StateMachineTaskV1 extends StatemachineTask<StateV1> {

        public StateMachineTaskV1() {
            this(StateV1.STAGE_INITIAL);
        }

        public StateMachineTaskV1(StateV1 stateV1) {
            super(stateV1, null);
        }

        private int value = 0;

        private void action() throws InterruptedException {
            value++;
            Thread.sleep(1000);
            System.out.println(value);
        }

        @Override
        protected Flow executeFromState(StateV1 stateV1) throws Exception {
            switch (stateV1) {
                case STAGE_INITIAL:
                    action();
                    setNextState(StateV1.STAGE_V1);
                    break;
                case STAGE_V1:
                    action();
                    setNextState(StateV1.STAGE_V2);
                    break;
                case STAGE_V2:
                    action();
                    setNextState(StateV1.STAGE_V3);
                    break;
                case STAGE_V3:
                    action();
                    setNextState(StateV1.STAGE_DONE);
                    break;
                case STAGE_DONE:
                    return Flow.HAS_NO_FLOW;
                default:
                    throw new Exception("invalid state");
            }
            return Flow.HAS_MORE_FLOW;
        }

        @Override
        protected Flow rollbackFromState(StateV1 stateV1) throws Exception {
            return null;
        }

        @Override
        public void rollback() {

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
    }
}
