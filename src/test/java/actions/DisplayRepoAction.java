package actions;

import org.junit.Test;
import xxx.joker.apps.video.manager.datalayer.VideoRepo;
import xxx.joker.apps.video.manager.datalayer.entities.Video;
import xxx.joker.libs.core.datetime.JkDuration;
import xxx.joker.libs.core.file.JkFiles;
import xxx.joker.libs.core.format.JkOutput;
import xxx.joker.libs.core.lambda.JkStreams;
import xxx.joker.libs.repo.design.entities.RepoResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static xxx.joker.libs.core.util.JkConsole.display;
import static xxx.joker.libs.core.util.JkStrings.strf;

public class DisplayRepoAction {

    @Test
    public void doDisplayResourcesDetails() throws IOException {
        VideoRepo repo = VideoRepo.getRepo();
        Set<Video> videos = repo.getVideos();
        List<RepoResource> resVideos = repo.findResources("snapshot");
        List<JkDuration> allSnaps = JkStreams.flatMap(videos, Video::getSnapTimes);
        List<String> lines = new ArrayList<>();
        lines.add(strf("Videos:|{}", resVideos.size()));
        lines.add(strf("Snap times:|{}", allSnaps.size()));
        List<Path> folders = JkFiles.findFolders(repo.getRepoCtx().getResourcesFolder(), false);
        for (Path folder : folders) {
            long num = Files.walk(folder).filter(Files::isRegularFile).count();
            lines.add(strf("{}|{}", folder.getFileName(), num));
        }
        display(JkOutput.columnsView(lines));
    }
}
