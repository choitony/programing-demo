package demo.machine.practice;

public enum DefaultStatemachineState{
    STAGE_INITIAL(0),
    STAGE_1(1),
    STAGE_2(2),
    STAGE_3(3),
    STAGE_DONE(4);

    private int code;

    DefaultStatemachineState(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static DefaultStatemachineState forCode(int code) {
        for (DefaultStatemachineState state : DefaultStatemachineState.values()) {
            if (state.getCode() == code) {
                return state;
            }
        }
        return null;
    }
}
