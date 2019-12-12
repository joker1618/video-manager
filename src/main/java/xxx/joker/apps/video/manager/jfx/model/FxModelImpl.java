package xxx.joker.apps.video.manager.jfx.model;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xxx.joker.apps.video.manager.datalayer.VideoRepo;
import xxx.joker.apps.video.manager.datalayer.entities.HistoricalFileHash;
import xxx.joker.apps.video.manager.datalayer.entities.Category;
import xxx.joker.apps.video.manager.datalayer.entities.Video;
import xxx.joker.apps.video.manager.jfx.view.PanesSelector;
import xxx.joker.libs.core.datetime.JkDuration;
import xxx.joker.libs.core.exception.JkThrowable;
import xxx.joker.libs.core.file.JkEncryption;
import xxx.joker.libs.core.file.JkFiles;
import xxx.joker.libs.core.lambda.JkStreams;
import xxx.joker.libs.core.test.JkTests;
import xxx.joker.libs.core.util.JkConvert;
import xxx.joker.libs.repo.design.entities.RepoResource;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static xxx.joker.libs.core.lambda.JkStreams.map;
import static xxx.joker.libs.core.lambda.JkStreams.sorted;
import static xxx.joker.libs.core.util.JkStrings.strf;

public class FxModelImpl implements FxModel {

    private static final Logger LOG = LoggerFactory.getLogger(FxModelImpl.class);

    private static final FxModel instance = new FxModelImpl();

    private VideoRepo repo = VideoRepo.getRepo();

    private final ObservableList<Video> videos;
    private final ObservableList<Category> categories;
    private final ObservableList<Video> selectedVideos;

    private FxModelImpl() {
        videos = FXCollections.observableArrayList(repo.getVideos());
        videos.sort(Video.titleComparator());
        categories = FXCollections.observableArrayList(repo.getCategories());
        selectedVideos = FXCollections.observableArrayList(new ArrayList<>());

        // for some reason, sometimes when a video is added, the length and the formats are not computed, so I check at start
        JkStreams.filterMap(videos, v -> v.getLength() == null, this::toFxVideo)
                .forEach(this::readVideoLengthWidthHeight);

        videos.addListener((ListChangeListener<? super Video>) lc -> {
            JkStreams.filter(videos, v -> !repo.getVideos().contains(v)).forEach(repo::add);
            JkStreams.filter(repo.getVideos(), v -> !videos.contains(v)).forEach(v -> {
                try {
//                    repo.remove(repo.getVideoResource(v));
                    v.getSnapTimes().forEach(st -> repo.remove(repo.getSnapshotResource(v, st)));
                    repo.remove(v);
                } catch (Exception ex) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(strf("Unable to delete video {}", v));
                    alert.setContentText(JkStreams.joinLines(Arrays.asList(ex.getStackTrace()), StackTraceElement::toString));
                    alert.showAndWait();
                    LOG.error("Unable to delete video {}\n{}", v, JkThrowable.toString(ex));
                    PanesSelector.getInstance().displayHomePane();
                }
            });
            List<Video> toDel = JkStreams.filter(selectedVideos, v -> !videos.contains(v));
            selectedVideos.removeAll(toDel);
        });

        categories.addListener((ListChangeListener<? super Category>) lc -> {
            JkStreams.filter(categories, c -> !repo.getCategories().contains(c)).forEach(repo::add);
            JkStreams.filter(repo.getCategories(), c -> !categories.contains(c)).forEach(repo::remove);
        });
    }

    static FxModel getInstance() {
        return instance;
    }


    @Override
    public ObservableList<Video> getVideos() {
        return videos;
    }

    @Override
    public ObservableList<Category> getCategories() {
        return categories;
    }

    @Override
    public ObservableList<Video> getSelectedVideos() {
        return selectedVideos;
    }

    @Override
    public FxVideo toFxVideo(Video video) {
        return new FxVideo(video);
    }

    @Override
    public List<FxVideo> toFxVideos(Collection<Video> videos) {
        return map(sorted(videos, Video.titleComparator()), FxVideo::new);
    }

    @Override
    public FxVideo addVideoFile(Path videoPath, boolean skipIfPreviouslyAdded) {
        Video video = createFromPath(videoPath);
        if(videos.contains(video)) {
            LOG.info("Skip add for video {}: already exists", videoPath);
            return null;
        }

        String finalTitle = computeSafeTitle(video.getTitle());
        video.setTitle(finalTitle);

        Set<HistoricalFileHash> addedFiles = repo.getAddedVideoHistory();
        HistoricalFileHash af = new HistoricalFileHash(video);
        if(skipIfPreviouslyAdded && addedFiles.contains(af)) {
            LOG.info("Skip add for video {}: previously added and deleted", videoPath);
            return null;
        }

        RepoResource vres = repo.addVideoResource(video, videoPath);
        video.setVideoResource(vres);
        videos.add(video);
        addedFiles.add(af);
        FxVideo fxVideo = new FxVideo(video);
        SimpleBooleanProperty finished = readVideoLengthWidthHeight(fxVideo);
        finished.addListener((obs,o,n) -> LOG.info("New video added {}", videoPath));
        return fxVideo;
    }

    @Override
    public String computeSafeTitle(String title) {
        List<String> titles = map(videos, Video::getTitle);
        int counter = 1;
        String finalTitle = title;
        while (JkTests.containsIgnoreCase(titles, finalTitle)) {
            finalTitle = strf("{}.{}", title, counter++);
        }
        return finalTitle;
    }

    @Override
    public List<FxSnapshot> getSnapshots(Video video) {
        return getSnapshots(video, -1);
    }

    @Override
    public List<FxSnapshot> getSnapshots(Video video, int numSnaps) {
        List<RepoResource> resList = repo.getSnapshotResources(video);
        List<FxSnapshot> toRet = JkStreams.mapSort(resList, r -> {
            FxSnapshot fxSnapshot = new FxSnapshot();
            fxSnapshot.setPath(r.getPath());
            fxSnapshot.setImage(new Image(JkFiles.toURL(r.getPath())));
            fxSnapshot.setTime(JkDuration.of(JkConvert.toLong(r.getName())));
            return fxSnapshot;
        });

        if(numSnaps != -1) {
            int end = Math.min(numSnaps, resList.size());
            return toRet.subList(0, end);
        } else {
            return toRet;
        }
    }

    @Override
    public void addSnapshot(Video video, JkDuration snapTime, Path snapPath) {
        repo.addSnapshotResource(video, snapTime, snapPath);
        JkFiles.delete(snapPath);
    }

    @Override
    public void removeSnapshot(Video video, JkDuration snapTime) {
        RepoResource res = repo.getSnapshotResource(video, snapTime);
        repo.remove(res);
    }

    @Override
    public void removeSnapshots(Video video) {
        List<RepoResource> resList = repo.getSnapshotResources(video);
        repo.removeAll(resList);
    }

    @Override
    public void persistData() {
        repo.commit();
    }

    private Video createFromPath(Path path)  {
        Video video = new Video();
        video.setMd5(JkEncryption.getMD5(path));
        video.setTitle(JkFiles.getFileName(path));
        video.setSize(JkFiles.sizeOf(path));
        return video;
    }

    private SimpleBooleanProperty readVideoLengthWidthHeight(FxVideo fxVideo) {
        MediaView mv = new MediaView();
        Media media = new Media(JkFiles.toURL(fxVideo.getPath()));
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setAutoPlay(false);
        mediaPlayer.setVolume(0d);
        mv.setMediaPlayer(mediaPlayer);
        AtomicInteger aint = new AtomicInteger(0);
        SimpleIntegerProperty iprop = new SimpleIntegerProperty(0);
        Video video = fxVideo.getVideo();
        mediaPlayer.totalDurationProperty().addListener((obs,o,n) -> { video.setLength(JkDuration.of(n)); iprop.set(aint.incrementAndGet());});
        media.widthProperty().addListener((obs,o,n) -> { video.setWidth(n.intValue()); iprop.set(aint.incrementAndGet());});
        media.heightProperty().addListener((obs,o,n) -> { video.setHeight(n.intValue()); iprop.set(aint.incrementAndGet());});
        SimpleBooleanProperty finished = new SimpleBooleanProperty(false);
        iprop.addListener((obs,o,n) -> { if(n.intValue() == 3) { mediaPlayer.stop(); mediaPlayer.dispose(); finished.setValue(true); }});
        mediaPlayer.play();
        finished.addListener((obs,o,n) -> LOG.debug("Set length, width and height for video {}", video.getTitle()));
        return finished;
    }
}
