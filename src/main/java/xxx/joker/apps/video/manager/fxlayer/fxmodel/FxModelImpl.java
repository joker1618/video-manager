package xxx.joker.apps.video.manager.fxlayer.fxmodel;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xxx.joker.apps.video.manager.datalayer.VideoRepo;
import xxx.joker.apps.video.manager.datalayer.entities.Category;
import xxx.joker.apps.video.manager.datalayer.entities.Video;
import xxx.joker.apps.video.manager.fxlayer.fxview.PanesSelector;
import xxx.joker.libs.core.datetime.JkDuration;
import xxx.joker.libs.core.files.JkEncryption;
import xxx.joker.libs.core.files.JkFiles;
import xxx.joker.libs.core.lambdas.JkStreams;
import xxx.joker.libs.datalayer.entities.RepoResource;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import static xxx.joker.libs.core.utils.JkConsole.displayColl;
import static xxx.joker.libs.core.utils.JkStrings.strf;

public class FxModelImpl implements FxModel {

    private static final Logger LOG = LoggerFactory.getLogger(FxModelImpl.class);

    private static final FxModel instance = new FxModelImpl();

    private VideoRepo repo = VideoRepo.getRepo();

    private final ObservableList<Video> videos;
    private final ObservableList<Category> categories;
    private final ObservableList<Video> selectedVideos;
    private final ObservableSet<Video> markedVideos;

    private FxModelImpl() {
        videos = FXCollections.observableArrayList(repo.getVideos());
        videos.sort(Comparator.comparing(v -> v.getTitle().toLowerCase()));
        categories = FXCollections.observableArrayList(repo.getCategories());
        selectedVideos = FXCollections.observableArrayList(new ArrayList<>());
        markedVideos = FXCollections.observableSet(new TreeSet<>());

        videos.addListener((ListChangeListener<? super Video>) lc -> {
            JkStreams.filter(videos, v -> !repo.getVideos().contains(v)).forEach(repo::add);
            JkStreams.filter(repo.getVideos(), v -> !videos.contains(v)).forEach(v -> {
                try {
                    repo.removeResource(repo.getVideoResource(v));
                    repo.remove(v);
                    v.getSnapTimes().forEach(st -> repo.removeResource(repo.getSnapshotResource(v, st)));
                } catch (Exception ex) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(strf("Unable to delete video {}", v));
                    alert.setContentText(JkStreams.joinLines(Arrays.asList(ex.getStackTrace()), StackTraceElement::toString));
                    alert.showAndWait();
                    PanesSelector.getInstance().displayHomePane();
                }
            });
            List<Video> toDel = JkStreams.filter(selectedVideos, v -> !videos.contains(v));
            selectedVideos.removeAll(toDel);
            persistData();
        });

        categories.addListener((ListChangeListener<? super Category>) lc -> {
            JkStreams.filter(categories, c -> !repo.getCategories().contains(c)).forEach(repo::add);
            JkStreams.filter(repo.getCategories(), c -> !categories.contains(c)).forEach(repo::remove);
            persistData();
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
    public FxVideo getFxVideo(Video video) {
        return new FxVideo(video, getVideoFile(video));
    }

    @Override
    public Path getVideoFile(Video video) {
        return repo.getVideoResource(video).getPath();
    }

    @Override
    public FxVideo addVideoFile(Path videoPath) {
        Video video = createFromPath(videoPath);
        if(videos.contains(video)) {
            LOG.info("Skip add for video {}: already exists", videoPath);
            return null;
        }
        FxVideo fxVideo = new FxVideo(video, videoPath);
        repo.addVideoResource(video, videoPath);
        SimpleBooleanProperty finished = readVideoLengthWidthHeight(fxVideo);
        videos.add(video);
        finished.addListener((obs,o,n) -> LOG.info("New video added {}", videoPath));
        return fxVideo;
    }

    @Override
    public FxSnapshot getSnapshot(Video video, JkDuration snapTime) {
        RepoResource res = repo.getSnapshotResource(video, snapTime);
        FxSnapshot snap = new FxSnapshot();
        snap.setPath(res.getPath());
        snap.setImage(new Image(JkFiles.toURL(res.getPath())));
        snap.setTime(snapTime);
        return snap;
    }

    @Override
    public void addSnapshot(Video video, JkDuration snapTime, Path snapPath) {
        repo.addSnapshotResource(video, snapTime, snapPath);
        JkFiles.delete(snapPath);
    }

    @Override
    public void removeSnapshot(Video video, JkDuration snapTime) {
        RepoResource res = repo.getSnapshotResource(video, snapTime);
        repo.removeResource(res);
    }

    @Override
    public void persistData() {
        repo.commit();
    }

    private Video createFromPath(Path path)  {
        Video video = new Video();
        video.setMd5(JkEncryption.getMD5(path));
        video.setTitle(JkFiles.getFileName(path));
        video.setSize(JkFiles.safeSize(path));
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
        return finished;
    }
}
