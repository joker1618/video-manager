package xxx.joker.apps.video.manager.common;

import xxx.joker.apps.video.manager.datalayer.entities.Video;
import xxx.joker.libs.core.datetime.JkDuration;
import xxx.joker.libs.core.runtime.JkEnvironment;
import xxx.joker.libs.core.runtime.JkRuntime;

import java.nio.file.Path;
import java.nio.file.Paths;

import static xxx.joker.libs.core.util.JkStrings.strf;

public class Config {

    public static final Path BASE_FOLDER;
    static {
        String baseFolderName = !JkRuntime.isRunFromJar(Config.class) ? "vm-bkp" : "video-manager";
        BASE_FOLDER = JkEnvironment.getAppsFolder().resolve(baseFolderName);
    }

//    public static final Path BASE_FOLDER = JkEnvironment.getAppsFolder().resolve("vm-bkp");
//    public static final Path BASE_FOLDER = JkEnvironment.getAppsFolder().resolve("video-manager");

    public static final Path REPO_FOLDER = BASE_FOLDER.resolve("repo");
    public static final String DB_NAME = "vm";

    public static final String CSV_STAGE_FILEPATH = "/data/MultiStagePositions.txt";
    public static final String REGEXP_TITLE_FILEPATH = "/data/RegexpVideoTitle.csv";

    public static final Path FOLDER_TEMP_SNAPS = BASE_FOLDER.resolve("tempSnaps");
    public static final Path FOLDER_TEMP_CUT = BASE_FOLDER.resolve("tempCut");

    public static final Path FFMPEG_EXE_PATH = Paths.get("exe/ffmpeg.exe");

    public static Path createSnapshotOutPath(Video video, JkDuration snapTime) {
        String fname = strf("{}_{}.snap.png", video.getMd5(), snapTime.toMillis());
        return FOLDER_TEMP_SNAPS.resolve(fname);
    }

}
