package xxx.joker.apps.video.manager.datalayer;

import xxx.joker.apps.video.manager.common.Config;
import xxx.joker.libs.datalayer.JkRepo;
import xxx.joker.libs.datalayer.JkRepoFile;

import java.nio.file.Path;

public class VideoRepoImpl extends JkRepoFile implements VideoRepo {

    public VideoRepoImpl() {
        super(Config.DB_FOLDER, Config.DB_NAME, "xxx.joker.apps.video.manager.datalayer.entities");
    }
}
