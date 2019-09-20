package xxx.joker.apps.video.manager.commonOK;

import xxx.joker.apps.video.manager.datalayer.entities.Video;
import xxx.joker.libs.core.datetime.JkDuration;
import xxx.joker.libs.core.runtimes.JkEnvironment;

import java.nio.file.Path;
import java.nio.file.Paths;

import static xxx.joker.libs.core.utils.JkStrings.strf;

public class Config {

    public static final Path BASE_FOLDER = JkEnvironment.getAppsFolder().resolve("vmx");
//    public static final Path BASE_FOLDER = JkEnvironment.getAppsFolder().resolve("video-manager");

    public static final Path REPO_FOLDER = BASE_FOLDER.resolve("repo");
    public static final String DB_NAME = "vm";

    public static final String CSV_STAGE_FILEPATH = "/data/MultiStagePositions.txt";

    public static final Path TEMP_FOLDER = BASE_FOLDER.resolve("temp");

    public static final Path FFMPEG_EXE_PATH = Paths.get("exe/ffmpeg.exe");

    public static Path createSnapshotOutPath(Video video, JkDuration snapTime) {
        String fname = strf("{}_{}.snap.png", video.getMd5(), snapTime.toMillis());
        return TEMP_FOLDER.resolve(fname);
    }

}
