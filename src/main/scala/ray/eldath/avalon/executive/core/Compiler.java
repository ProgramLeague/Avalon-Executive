package ray.eldath.avalon.executive.core;

import com.spotify.docker.client.exceptions.DockerException;
import ray.eldath.avalon.executive.exception.CompileErrorException;
import ray.eldath.avalon.executive.model.ExecState;
import ray.eldath.avalon.executive.model.Language;
import ray.eldath.avalon.executive.model.Submission;
import ray.eldath.avalon.executive.pool.CompilerContainerPool;
import ray.eldath.avalon.executive.tool.DockerOperator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import static ray.eldath.avalon.executive.core.PreProcessor.getCodeFileSuffix;

public class Compiler {
    public static File compile(String workDir, Submission submission, File codeFile)
            throws DockerException, InterruptedException, IOException, CompileErrorException {
        Language language = submission.getLanguage();
        String executableSuffix = getCodeFileSuffix(language);
        File executableFile = new File(String.format("%s/%s/_file.%s", workDir, submission.getSubmitTime(), executableSuffix));
        String containerId = CompilerContainerPool.instance().getContainerId(language);
        String cmd = language.getCompileCmd();

        DockerOperator.instance().copyFileIn(containerId, Paths.get(codeFile.getParent()), ".");
        ExecState state = DockerOperator.instance().exec(containerId, new String[]{cmd});
        if (state.getExitCode() != 0)
            throw new CompileErrorException(state);
        DockerOperator.instance().copyFileOut(containerId, executableFile, "_file" + executableSuffix);
        return executableFile;
    }
}
