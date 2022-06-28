package demo.machine.practice;

import demo.machine.executor.SimpleTaskExecutor;
import demo.machine.task.Flow;

/**
 * 支持子任务生成。
 */
public class TestV2 {

    static enum Stage_V2 {
        STAGE_INITIAL,
        STAGE_1,
        STAGE_2,
        STAGE_DONE;
    }

    static class StatemachineTaskV2 extends demo.machine.task.StatemachineTask<Stage_V2> {

        int value;

        public StatemachineTaskV2() {
            this(Stage_V2.STAGE_INITIAL);
        }

        public StatemachineTaskV2(Stage_V2 initalState) {
            super(initalState, null);
        }

        private void action() throws InterruptedException {
            value++;
            Thread.sleep(1000);
            System.out.println(value);
        }

        /**
         * 在STAGE_2添加子任务。
         *
         * @param stage_v2
         * @return
         * @throws Exception
         */
        @Override
        protected Flow executeFromState(Stage_V2 stage_v2) throws Exception {

            switch (stage_v2) {
                case STAGE_INITIAL:
                    action();
                    setNextState(Stage_V2.STAGE_1);
                    break;
                case STAGE_1:
                    action();
                    addChildrenTask(new SonTask("son1", this));
                    addChildrenTask(new SonTask("son2", this));
                    setNextState(Stage_V2.STAGE_2);
                    break;
                case STAGE_2:
                    action();
                    setNextState(Stage_V2.STAGE_DONE);
                    break;
                case STAGE_DONE:
                    System.out.println("DONE");
                    return Flow.HAS_NO_FLOW;
                default:
                    throw new Exception("invalid state");
            }
            return Flow.HAS_MORE_FLOW;
        }

        @Override
        protected Flow rollbackFromState(Stage_V2 stage_v2) throws Exception {
            return null;
        }
    }

    public static void main(String[] args) {
        SimpleTaskExecutor executor = new SimpleTaskExecutor();
        executor.submit(new StatemachineTaskV2());
        executor.shutDownGracefully();
    }

}
