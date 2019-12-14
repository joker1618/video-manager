package operations;

import org.junit.Test;
import xxx.joker.apps.video.manager.datalayer.VideoRepo;
import xxx.joker.apps.video.manager.datalayer.VideoRepoImpl;
import xxx.joker.apps.video.manager.datalayer.entities.Video;
import xxx.joker.apps.video.manager.datalayer.entities.VideoSnap;
import xxx.joker.apps.video.manager.jfx.model.FxModel;
import xxx.joker.apps.video.manager.jfx.model.FxSnapshot;
import xxx.joker.libs.core.datetime.JkDuration;
import xxx.joker.libs.core.util.JkConvert;
import xxx.joker.libs.repo.design.entities.RepoResource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static xxx.joker.libs.core.lambda.JkStreams.map;
import static xxx.joker.libs.core.util.JkConsole.display;

public class PutSnapshotsInVideo {

    @Test
    public void putSnapshotsInVideo() {
//        VideoRepoImpl repo = (VideoRepoImpl) VideoRepo.getRepo();
//        AtomicInteger c = new AtomicInteger(1);
//        for (Video video : repo.getVideos()) {
//            display("{}/{}", c.getAndIncrement(), repo.getVideos().size());
//            List<VideoSnap> videoSnaps = new ArrayList<>();
//            for (RepoResource sr : repo.getSnapshotResources(video)) {
//                JkDuration stime = JkDuration.of(JkConvert.toLong(sr.getName()));
//                VideoSnap vs = new VideoSnap(video.getMd5(), stime, repo.getSnapshotResource(video, stime));
//                videoSnaps.add(vs);
//            }
//            video.getSnapshots().clear();
//            video.getSnapshots().addAll(videoSnaps);
//        }
//
//        display(repo.toStringClass(true, Video.class));
//
////        repo.commit();
//        display("Added snapshots to videos");
    }
}
