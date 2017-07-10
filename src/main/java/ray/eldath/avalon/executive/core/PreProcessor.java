package ray.eldath.avalon.executive.core;

import com.spotify.docker.client.exceptions.DockerException;
import org.apache.commons.io.IOUtils;
import ray.eldath.avalon.executive.model.Language;
import ray.eldath.avalon.executive.model.Submission;
import ray.eldath.avalon.executive.pool.CompilerContainerPool;
import ray.eldath.avalon.executive.tool.DockerOperator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PreProcessor {
    private static final PreProcessor instance = new PreProcessor();

    public static PreProcessor instance() {
        return instance;
    }

    public File createFile(String workDirectory, Submission submission)
            throws IOException, DockerException, InterruptedException {
        File workDirFile = new File(workDirectory);

        if (!workDirFile.exists() && !workDirFile.mkdirs())
            throw new IOException("failed to create directory: " + workDirectory);

        Language language = submission.getLanguage();
        String filePath = workDirectory + "/" + String.valueOf(submission.getSubmitTime());
        String codeFilePath = String.format("%s/_file.%s", filePath, getCodeFileSuffix(language.getCompileCmd()));

        String[] codeLinesArray = replaceClassName(language, submission.getDecodedCode())
                .replace("\r", "")
                .split("\n");

        Files.createDirectories(Paths.get(codeFilePath).getParent());

        BufferedWriter writer = new BufferedWriter(new FileWriter(codeFilePath));
        for (String thisLineCode : codeLinesArray) {
            writer.write(thisLineCode);
            writer.newLine();
        }
        IOUtils.closeQuietly(writer);

        String containerId = CompilerContainerPool.instance().getContainerId(language);
        DockerOperator.instance().copyFileIn(
                containerId,
                Paths.get(codeFilePath).getParent(),
                "/sandbox");

        Files.deleteIfExists(Paths.get(codeFilePath));
        return new File(codeFilePath);
    }

    /**
     * Copied from voj.
     * <p>
     * 替换部分语言中的类名(如Java), 以保证正常通过编译.
     *
     * @param language - 编程语言对象
     * @param code     - 待替换的代码
     */
    private String replaceClassName(Language language, String code) {
        if (!language.getName().toLowerCase().equals("java"))
            return code;
        return code.replaceAll("class[ \n]+Main", "class _file");
    }

    static String getCodeFileSuffix(String cmd) {
        Matcher matcher = Pattern.compile("_file\\.((?!exe| ).)+").matcher(cmd);
        return matcher.find() ? matcher.group().replace("_file.", "") : "";
    }
}
