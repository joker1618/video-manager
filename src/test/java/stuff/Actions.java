package stuff;

import org.junit.Test;
import xxx.joker.apps.video.manager.action.AdjustVideoTitleAction;
import xxx.joker.apps.video.manager.datalayer.VideoRepo;
import xxx.joker.apps.video.manager.datalayer.entities.Video;
import xxx.joker.libs.core.file.JkFiles;
import xxx.joker.libs.core.lambda.JkStreams;

import java.nio.file.Path;
import java.util.List;

import static xxx.joker.libs.core.util.JkConsole.display;
import static xxx.joker.libs.core.util.JkConsole.displayColl;

public class Actions {

    @Test
    public void cleanRepoAction() {
        VideoRepo repo = VideoRepo.getRepo();
        repo.cleanRepo();
        display("END");
    }

    @Test
    public void changeVideoTitlesAction() {
        VideoRepo repo = VideoRepo.getRepo();
        AdjustVideoTitleAction.adjustTitles(repo.getVideos());
//        repo.commit();
        display("END");
    }
}
