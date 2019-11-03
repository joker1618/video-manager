package stuff;

import org.junit.Test;
import xxx.joker.apps.video.manager.datalayer.VideoRepo;
import xxx.joker.apps.video.manager.datalayer.entities.Video;
import xxx.joker.libs.core.datetime.JkDuration;
import xxx.joker.libs.core.files.JkFiles;
import xxx.joker.libs.core.format.JkOutput;
import xxx.joker.libs.core.lambdas.JkStreams;
import xxx.joker.libs.datalayer.entities.RepoResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static xxx.joker.libs.core.utils.JkConsole.display;
import static xxx.joker.libs.core.utils.JkConsole.displayColl;
import static xxx.joker.libs.core.utils.JkStrings.strf;

public class Displayer {

    @Test
    public void checkSnapshots() throws IOException {
        VideoRepo repo = VideoRepo.getRepo();
        Set<Video> videos = repo.getVideos();
        List<RepoResource> resVideos = repo.findResources("snapshot");
        List<JkDuration> allSnaps = JkStreams.flatMap(videos, Video::getSnapTimes);
        List<String> lines = new ArrayList<>();
        lines.add(strf("Resources:|{}", resVideos.size()));
        lines.add(strf("Snap times:|{}", allSnaps.size()));
        List<Path> folders = JkFiles.findFolders(repo.getRepoCtx().getResourcesFolder(), false);
        for (Path folder : folders) {
            long num = Files.walk(folder).filter(Files::isRegularFile).count();
            lines.add(strf("{}|{}", folder.getFileName(), num));
        }
        display(JkOutput.columnsView(lines));
    }
}