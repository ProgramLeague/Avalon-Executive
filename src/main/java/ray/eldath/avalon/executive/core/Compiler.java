package ray.eldath.avalon.executive.core;

import com.spotify.docker.client.exceptions.DockerException;
import ray.eldath.avalon.executive.exception.CompileErrorException;
import ray.eldath.avalon.executive.model.ExecInfoSimple;
import ray.eldath.avalon.executive.model.ExecPair;
import ray.eldath.avalon.executive.model.Language;
import ray.eldath.avalon.executive.model.Submission;
import ray.eldath.avalon.executive.pool.CompilerContainerPool;
import ray.eldath.avalon.executive.tool.DockerOperator;

import java.io.File;
import java.io.IOException;

import static ray.eldath.avalon.executive.core.PreProcessor.getCodeFileSuffix;

public class Compiler {
    public static File compile(String workDir, Submission submission)
            throws DockerException, InterruptedException, IOException, CompileErrorException {
        Language language = submission.getLanguage();
        String cmd = language.getCompileCmd();
        String executableSuffix = getCodeFileSuffix(cmd);
        File executableFile = new File(String.format("%s/%s/_file.%s", workDir, submission.getSubmitTime(), executableSuffix));
        String containerId = CompilerContainerPool.instance().getContainerId(language);

        ExecPair state = DockerOperator.instance().exec(containerId, cmd);
        ExecInfoSimple info = DockerOperator.instance().inspectExec(state.getExecId());
        if (info.getExitCode() != 0)
            throw new CompileErrorException(state);
        DockerOperator.instance().copyFileOut(containerId, executableFile, "/sandbox/_file." + executableSuffix);
        return executableFile;
    }
}
