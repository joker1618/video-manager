package xxx.joker.apps.video.manager.commonOK;

import xxx.joker.libs.core.runtimes.JkEnvironment;

import java.nio.file.Path;

public class Config {

    public static final Path BASE_FOLDER = JkEnvironment.getAppsFolder().resolve("v2");

    public static final Path REPO_FOLDER = BASE_FOLDER.resolve("repo");
    public static final String DB_NAME = "vm";

    public static final String CSV_STAGE_FILEPATH = "/data/MultiStagePositions.txt";

}
