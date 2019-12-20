package modelChanges;

import org.junit.Test;

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
