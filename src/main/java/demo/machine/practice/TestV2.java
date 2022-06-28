package demo.machine.practice;

import demo.machine.executor.SimpleTaskExecutor;
import demo.machine.task.Flow;

import static demo.machine.practice.DefaultStatemachineState.STAGE_2;

/**
 * 支持子任务生成。
 */
public class TestV2 {

    static class StatemachineTaskV2 extends demo.machine.task.StatemachineTask<DefaultStatemachineState> {

        int value;

        public StatemachineTaskV2() {
            this(STAGE_2.STAGE_INITIAL);
        }

        public StatemachineTaskV2(DefaultStatemachineState initalState) {
            super(initalState, null);
        }

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
        public DefaultStatemachineState getState(int id) {
            return DefaultStatemachineState.forCode(id);
        }

        @Override
        public int getStateCode(DefaultStatemachineState defaultStatemachineState) {
            return defaultStatemachineState.getCode();
        }

        /**
         * 在STAGE_2添加子任务。
         *
         * @param DefaultStatemachineState
         * @return
         * @throws Exception
         */
        @Override
        protected Flow executeFromState(DefaultStatemachineState DefaultStatemachineState) {

            switch (DefaultStatemachineState) {
                case STAGE_INITIAL:
                    action();
                    setNextState(DefaultStatemachineState.STAGE_1);
                    break;
                case STAGE_1:
                    action();
                    addChildrenTask(new SonTask("son1", this));
                    addChildrenTask(new SonTask("son2", this));
                    setNextState(DefaultStatemachineState.STAGE_2);
                    break;
                case STAGE_2:
                    action();
                    setNextState(DefaultStatemachineState.STAGE_DONE);
                    break;
                case STAGE_DONE:
                    System.out.println("DONE");
                    return Flow.HAS_NO_FLOW;
                default:
                    ;
            }
            return Flow.HAS_MORE_FLOW;
        }

        @Override
        protected Flow rollbackFromState(DefaultStatemachineState DefaultStatemachineState) throws Exception {
            return null;
        }
    }

    public static void main(String[] args) {
        SimpleTaskExecutor executor = new SimpleTaskExecutor();
        executor.submit(new StatemachineTaskV2());
        executor.shutDownGracefully();
    }

}
