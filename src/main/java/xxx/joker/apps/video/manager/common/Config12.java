package xxx.joker.apps.video.manager.common;

import xxx.joker.apps.video.manager.datalayer.entities.Video;
import xxx.joker.libs.core.runtimes.JkEnvironment;

import java.nio.file.Path;
import java.nio.file.Paths;

import static xxx.joker.libs.core.utils.JkStrings.strf;

public class Config12 {

	public static final Path BASE_FOLDER = JkEnvironment.getAppsFolder().resolve("video-manager");

	public static final Path DB_FOLDER = BASE_FOLDER.resolve("repo");
	public static final String DB_NAME = "vm";

	public static final Path SNAPSHOT_FOLDER = BASE_FOLDER.resolve("snapshots");

	public static final String CSV_STAGE_FILEPATH = "/data/MultiStagePositions.txt";

	public static Path createSnapshotOutPath(Video video, long snapTime) {
		String fname = strf("{}_{}.snap.png", video.getMd5(), snapTime);
		return SNAPSHOT_FOLDER.resolve(fname);
	}
}
