package ray.eldath.avalon.executive.core;

import org.apache.commons.io.IOUtils;
import ray.eldath.avalon.executive.model.Language;
import ray.eldath.avalon.executive.model.Submission;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PreProcessor {

    public void createFile(String workDirectory, Submission submission) throws IOException {
        File workDirFile = new File(workDirectory);

        if (!workDirFile.exists() && !workDirFile.mkdirs())
            throw new IOException("failed to create directory: " + workDirectory);

        Language language = submission.getLanguage();
        String fileName = String.valueOf(submission.getSubmitTime());
        String codeFilePath = String.format("%s/%s.%s", workDirectory, fileName, getCodeFileSuffix(language));
        String[] codeLinesArray = replaceClassName(language, submission.getDecodedCode(), fileName)
                .replace("\r", "")
                .split("\n");

        BufferedWriter writer = new BufferedWriter(new FileWriter(codeFilePath));
        for (String thisLineCode : codeLinesArray) {
            writer.write(thisLineCode);
            writer.newLine();
        }

        IOUtils.closeQuietly(writer);
    }

    /**
     * Copied from voj.
     * <p>
     * 替换部分语言中的类名(如Java), 以保证正常通过编译.
     *
     * @param language     - 编程语言对象
     * @param code         - 待替换的代码
     * @param newClassName - 新的类名
     */
    private String replaceClassName(Language language, String code, String newClassName) {
        if (!language.getName().toLowerCase().equals("java"))
            return code;
        return code.replaceAll("class[ \n]+Main", "class " + newClassName);
    }

    private String getCodeFileSuffix(Language language) {
        String compileCmd = language.getCompileCmd();
        Matcher matcher = Pattern.compile("\\{file}\\.((?!exe| ).)+").matcher(compileCmd);
        return matcher.find() ? matcher.group().replace("{file}.", "") : "";
    }
}
