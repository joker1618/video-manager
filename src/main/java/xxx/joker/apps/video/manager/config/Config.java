package xxx.joker.apps.video.manager.config;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Config {

	public static final Path BASE_FOLDER = Paths.get("files");

	public static final Path DATA_FOLDER = BASE_FOLDER.resolve("data");

	public static final Path CSV_CATEGORIES = DATA_FOLDER.resolve("categories.csv");
	public static final Path CSV_VIDEOS = DATA_FOLDER.resolve("videos.csv");

//	public static final Path VIDEOS_FOLDER = Paths.get("C:\\Users\\f.barbano\\Desktop\\VMtemp\\vman");
	public static final Path VIDEOS_FOLDER = Paths.get("videos");

	public static final Path CSV_STAGE_POSITIONS = DATA_FOLDER.resolve("MultiStagePositions.csv");
}