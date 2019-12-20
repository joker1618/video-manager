package actions;

import org.junit.Test;
import xxx.joker.apps.video.manager.datalayer.VideoRepo;

import java.nio.file.Path;
import java.util.List;

import static xxx.joker.libs.core.util.JkConsole.display;

public class CleanRepoAction {

    @Test
    public void doCleanRepo() {
        VideoRepo repo = VideoRepo.getRepo();
        List<Path> pathList = repo.cleanResources();
        display("{} files removed", pathList.size());
        display("END");
    }

}
