package xxx.joker.apps.video.manager.config;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Config {

	public static final Path HOME = Paths.get(System.getProperty("user.home"));
	public static final Path BASE_FOLDER = HOME.resolve("IdeaProjects\\APPS\\video-manager\\files");

	public static final Path DATA_FOLDER = BASE_FOLDER.resolve("data");

	public static final Path DB_FOLDER = BASE_FOLDER.resolve("db");
	public static final String DB_NAME = "dbvideomanager";

	public static final Path VIDEOS_FOLDER = BASE_FOLDER.resolve("videos");
//	public static final Path VIDEOS_FOLDER = Paths.get("videos");

	public static final Path CSV_STAGE_POSITIONS = DATA_FOLDER.resolve("MultiStagePositions.csv");
}
