package ray.eldath.avalon.executive.core;

import org.apache.commons.io.IOUtils;
import ray.eldath.avalon.executive.model.Language;
import ray.eldath.avalon.executive.model.Submission;
import ray.eldath.avalon.executive.pool.Constants;

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

	public File createFile(Submission submission) throws IOException {
		File workDirFile = new File(Constants._WORK_DIR());

		if (!workDirFile.exists() && !workDirFile.mkdirs())
			throw new IOException("failed to create directory: " + Constants._WORK_DIR());

		Language language = submission.getLanguage();
		String codeFilePath = String.format("%s/%s.%s", Constants._WORK_DIR(),
				submission.getSubmitTime(), getCodeFileSuffix(language.getCompileCmd()));

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
