package actions;

import org.junit.Test;
import xxx.joker.apps.video.manager.datalayer.VideoRepo;
import xxx.joker.libs.core.file.JkFiles;
import xxx.joker.libs.core.runtime.JkEnvironment;
import xxx.joker.libs.repo.design.entities.RepoTags;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import static xxx.joker.libs.core.util.JkConsole.display;

public class ExportVideosAction {

    @Test
    public void doExportVideos() {

        Path folder = JkEnvironment.getDesktopFolder().resolve("vidddds");
        VideoRepo repo = VideoRepo.getRepo();
        AtomicInteger counter = new AtomicInteger(1);
        repo.getVideos().forEach(video -> {
            Path resPath = video.getVideoResource().getPath();
            Path outPath = folder.resolve(video.getTitle() + JkFiles.getExtension(resPath, true));
            JkFiles.copy(resPath, outPath);
            display("{}/{}", counter.getAndIncrement(), repo.getVideos().size());
        });

    }
}
