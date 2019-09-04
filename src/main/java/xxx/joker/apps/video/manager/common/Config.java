package xxx.joker.apps.video.manager.common;

import xxx.joker.apps.video.manager.model.entity.Video;
import xxx.joker.libs.core.datetime.JkTime;
import xxx.joker.libs.core.utils.JkFiles;

import java.nio.file.Path;
import java.nio.file.Paths;

import static xxx.joker.libs.core.utils.JkStrings.strf;

public class Config {

	public static final Path HOME = Paths.get(System.getProperty("user.home"));
	public static final Path BASE_FOLDER = HOME.resolve(".appsFolder/video_manager");

	public static final Path DB_FOLDER = BASE_FOLDER.resolve("db");
	public static final String DB_NAME = "dbvideomanager";

	public static final Path VIDEOS_FOLDER = BASE_FOLDER.resolve("videos");
	public static final Path SNAPSHOT_FOLDER = BASE_FOLDER.resolve("snapshots");

	public static final String CSV_STAGE_FILEPATH = "/data/MultiStagePositions.txt";

	public static Path createSnapshotOutPath(Video video, long snapTime) {
		String fname = strf("{}_{}.snap.png", video.getMd5(), snapTime);
		return SNAPSHOT_FOLDER.resolve(fname);
	}
}
