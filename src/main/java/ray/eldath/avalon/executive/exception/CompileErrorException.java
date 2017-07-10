package ray.eldath.avalon.executive.exception;

import ray.eldath.avalon.executive.model.ExecPair;

public class CompileErrorException extends Exception {
    private ExecPair state;

    public CompileErrorException(ExecPair state) {
        this.state = state;
    }

    public ExecPair getState() {
        return state;
    }

    @Override
    public String toString() {
        return "CompileErrorException{" +
                "state=" + state.toString() +
                '}';
    }
}
