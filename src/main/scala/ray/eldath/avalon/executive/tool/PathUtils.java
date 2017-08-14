package ray.eldath.avalon.executive.tool;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class PathUtils {
	public static void deleteAll(Path path) {
		try {
			List<Path> root = Files.list(path).collect(toList());
			for (Path t : root) {
				delete(t);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static void delete(Path path) throws IOException {
		List<Path> file = Files.list(path).collect(toList());
		for (Path t : file) {
			if (Files.isDirectory(t))
				delete(t);
			else
				Files.delete(t);
		}
	}
}
