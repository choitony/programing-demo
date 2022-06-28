package demo.machine.practice;

import demo.machine.executor.SimpleTaskExecutor;
import demo.machine.task.Flow;
import demo.machine.task.StatemachineTask;
import demo.machine.task.Task;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 实现如下回滚功能：
 * 1、设计定义一个RooBackTask，具备如下执行逻辑：第一个阶段对value进行加1，第二阶段生成3个子任务，对value分别乘以2，第三个阶段对value进行加2；
 * 2、回滚要求：
 * 2.1 子任务可以回滚：   一个子任务失败，其他子任务会处于三种状态：完成、运行中，未运行。对于未运行的，直接abort掉，不用回滚；对于运行中的应该直接失败掉，进入回滚流程。对于完成的，需要重新启动进入回滚逻辑。
 * 2.2 父任务也可以回滚： 回滚到上一个阶段，如果上一阶段中有子任务生成，则生成所有子任务，并且提交回滚操作，并等待所有子任务完成后继续运行。
 * 2.3 回滚的最终结果都是value恢复到初始值
 * 3、子任务间没有执行顺序依赖。
 */

public class TestV4 {

    /*enum State_V4 {
        INITIAL,
        ADD_1,
        MULTI_2,
        ADD_2,
        DONE;
    }

    static class SonTask_V4 extends Task {

        private final AtomicInteger value;

        public SonTask_V4(AtomicInteger value) {
            super(null);
            this.value = value;
        }

        @Override
        public Task[] execute() throws Exception {
            if (isRunnable()) {
                lifecycle = Lifecycle.RUNNING;
            }

            if (isRollBack()) {
                rollback();
            } else {

                int pre;
                do {
                    pre = value.get();
                } while (!value.compareAndSet(pre, pre * 2));
            }

            if (isRunning()) {
                complete();
            }
            return new Task[0];
        }

        @Override
        public void rollback() {
            int pre;
            do {
                pre = value.get();
            } while (!value.compareAndSet(pre, pre / 2));
        }
    }

    static class RollBackTask extends StatemachineTask<demo.machine.practice.TestV4.State_V4> {

        AtomicInteger value = new AtomicInteger(2);

        public RollBackTask() {
            this(demo.machine.practice.TestV4.State_V4.INITIAL);
        }

        public RollBackTask(demo.machine.practice.TestV4.State_V4 state) {
            super(state, null);
        }

        @Override
        protected Flow executeFromState(demo.machine.practice.TestV4.State_V4 state_v4) throws Exception {

            switch (state_v4) {
                case INITIAL:
                    setNextState(demo.machine.practice.TestV4.State_V4.ADD_1);
                    break;
                case ADD_1:
                    value.incrementAndGet();
                    setNextState(demo.machine.practice.TestV4.State_V4.MULTI_2);
                    break;
                case MULTI_2:
                    addChildrenTask(new SonTask_V4(value));
                    addChildrenTask(new SonTask_V4(value));
                    setNextState(demo.machine.practice.TestV4.State_V4.ADD_2);
                    break;
                case ADD_2:
//                    if (value.get() == 6)
//                        throw new Exception("task = " + getID() + " ADD_2 failed, please rollback, current value = " + value);
                    value.incrementAndGet();
                    value.incrementAndGet();
                    setNextState(demo.machine.practice.TestV4.State_V4.DONE);
                    break;
                case DONE:
                    complete();
                    System.out.println("task done with value = " + value);
                    return Flow.HAS_NO_FLOW;
                default:
                    throw new IllegalAccessException("invalid state" + state_v4);
            }

            return Flow.HAS_MORE_FLOW;
        }

        @Override
        public Flow rollbackFromState(demo.machine.practice.TestV4.State_V4 state_v4) throws Exception {

            switch (state_v4) {
                case INITIAL:
                    System.out.println("roll back done with value = " + value);
                    return Flow.HAS_NO_FLOW;
                case ADD_1:
                    setNextState(demo.machine.practice.TestV4.State_V4.INITIAL);
                    break;
                case MULTI_2:
                    value--;
                    setNextState(demo.machine.practice.TestV4.State_V4.ADD_1);
                    break;
                case ADD_2:
                    for (int i = 0; i < 2; i++) {
                        SonTask_V4 v4 = new SonTask_V4(value);
                        v4.setRollBack();
                        addChildrenTask(v4);
                    }
                    setNextState(demo.machine.practice.TestV4.State_V4.MULTI_2);
                    break;
                case DONE:
                    setNextState(demo.machine.practice.TestV4.State_V4.ADD_2);
                default:
                    throw new IllegalAccessException("invalid state" + state_v4);
            }
            return Flow.HAS_MORE_FLOW;
        }
    }

    public static void main(String[] args) {
        SimpleTaskExecutor ste = new SimpleTaskExecutor();
        ste.submit(new demo.machine.practice.TestV4.RollBackTask());
        ste.shutDownGracefully();
    }*/
}
