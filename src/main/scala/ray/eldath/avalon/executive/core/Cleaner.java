package ray.eldath.avalon.executive.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ray.eldath.avalon.executive.pool.Constants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Cleaner extends Thread {
	private static final Logger LOGGER = LoggerFactory.getLogger(Cleaner.class);

	@Override
	public void run() {
		try {
			LOGGER.info("Now cleaning temp files...");
			final List<Path> toRemove = new ArrayList<>();
			final long current = System.currentTimeMillis() - 2 * Runner._TIME_LIMIT_MILLISECONDS;
			Files.list(Paths.get(Constants._WORK_DIR())).forEach(e -> {
				if (Long.parseLong(e.getFileName().toString().replaceAll(".\\w+", "")) < current)
					toRemove.add(e);
			});
			for (Path t : toRemove)
				Files.deleteIfExists(t);
			LOGGER.info("Clean process finished.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
