package ray.eldath.avalon.executive.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Cleaner {
	public static void clean(File codeFile, File executableFile) throws IOException {
		Files.deleteIfExists(codeFile.toPath());
		Files.deleteIfExists(executableFile.toPath());
	}
}
