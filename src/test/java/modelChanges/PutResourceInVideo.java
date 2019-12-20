package modelChanges;

import org.junit.Test;
import xxx.joker.apps.video.manager.datalayer.VideoRepo;
import xxx.joker.apps.video.manager.datalayer.VideoRepoImpl;
import xxx.joker.apps.video.manager.datalayer.entities.Video;
import xxx.joker.libs.repo.design.entities.RepoResource;

import java.util.Map;
import java.util.Set;

import static xxx.joker.libs.core.util.JkConsole.display;

public class PutResourceInVideo {

    @Test
    public void putResourceInVideo() {
        VideoRepoImpl repo = (VideoRepoImpl) VideoRepo.getRepo();
        Set<Video> videos = repo.getVideos();
        Map<Video, RepoResource> resMap = repo.getVideoResources(videos);
        resMap.forEach((v,r) -> v.setVideoResource(r));
        repo.commit();
        display("Added video resource files to videos");
    }
}
