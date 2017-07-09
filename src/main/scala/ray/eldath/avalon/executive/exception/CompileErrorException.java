package ray.eldath.avalon.executive.exception;

import ray.eldath.avalon.executive.model.ExecState;

public class CompileErrorException extends Exception {
    private ExecState state;

    public CompileErrorException(ExecState state) {
        this.state = state;
    }

    public ExecState getState() {
        return state;
    }
}
