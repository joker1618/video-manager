package xxx.joker.apps.video.manager.fxlayer.fxview.builders;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.*;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xxx.joker.apps.video.manager.datalayer.entities.Video;
import xxx.joker.apps.video.manager.fxlayer.fxmodel.FxModel;
import xxx.joker.apps.video.manager.fxlayer.fxmodel.FxVideo;
import xxx.joker.apps.video.manager.fxlayer.fxview.videoplayer.JfxVideoBuilder;
import xxx.joker.apps.video.manager.fxlayer.fxview.videoplayer.JfxVideoPlayer;
import xxx.joker.apps.video.manager.fxlayer.fxview.videoplayer.JfxVideoStage;
import xxx.joker.libs.core.datetime.JkDuration;
import xxx.joker.libs.core.lambdas.JkStreams;
import xxx.joker.libs.core.utils.JkConvert;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static xxx.joker.libs.core.utils.JkConsole.display;
import static xxx.joker.libs.core.utils.JkStrings.strf;

public class SnapshotManager {

    private static final Logger LOG = LoggerFactory.getLogger(SnapshotManager.class);

    private FxModel model = FxModel.getModel();

    public SnapshotManager() {
    }

    public SimpleBooleanProperty runAutoSnap(Collection<Video> videos) {
        List<FxVideo> fxVideos = JkStreams.map(videos, model::getFxVideo);
        Pair<SnapType, Integer> pair = askAutoSnapType();
        if(pair != null) {
            Integer num = pair.getValue();
            if(num == null || num <= 0) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText(strf("Invalid number: {}", num));
                alert.showAndWait();
            } else {
                JfxVideoBuilder videoBuilder = new JfxVideoBuilder();
                videoBuilder.setShowBorder(true);
                JfxVideoStage stage = videoBuilder.createStage();
                stage.setMaximized(true);
                SimpleBooleanProperty finished = new SimpleBooleanProperty(false);
                takeSnapshots(stage, fxVideos, pair, finished);
                return finished;
            }
        }
        return null;
    }

    private Pair<SnapType, Integer> askAutoSnapType() {
        Dialog<ButtonType> dlg = new Dialog<>();
        GridPaneBuilder gpBuilder = new GridPaneBuilder();
        RadioButton rbSnap = new RadioButton("Number of snapshots");
        TextField txtNumSnap = new TextField("16");
        TextField txtNumSec = new TextField("20");
        RadioButton rbSec = new RadioButton("Every X seconds");
        txtNumSnap.disableProperty().bind(rbSec.selectedProperty());
        txtNumSec.disableProperty().bind(rbSnap.selectedProperty());
        ToggleGroup tg = new ToggleGroup();
        tg.getToggles().addAll(rbSnap, rbSec);
        rbSnap.setSelected(true);
        gpBuilder.add(0, 0, txtNumSnap);
        gpBuilder.add(0, 1, rbSnap);
        gpBuilder.add(1, 0, txtNumSec);
        gpBuilder.add(1, 1, rbSec);
        DialogPane dialogPane = new DialogPane();
        dialogPane.setContent(gpBuilder.createGridPane());
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dlg.setDialogPane(dialogPane);
        Optional<ButtonType> optional = dlg.showAndWait();
        if(optional.isPresent() && optional.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
            String str = rbSnap.isSelected() ? txtNumSnap.getText() : txtNumSec.getText();
            SnapType snapType = rbSnap.isSelected() ? SnapType.SNAPS_NUMBER : SnapType.SECONDS_BETWEEN_SNAPS;
            Integer num = JkConvert.toInt(str);
            return Pair.of(snapType, num);
        }
        return null;
    }

    private void takeSnapshots(JfxVideoStage stage, List<FxVideo> fxVideos, Pair<SnapType, Integer> pairChoose, SimpleBooleanProperty finished) {
        if(fxVideos.isEmpty()) {
            stage.close();
            model.persistData();
            finished.setValue(true);
        } else {
            FxVideo fxVideo = fxVideos.remove(0);

            List<Long> times = new ArrayList<>();
            Integer numChoosed = pairChoose.getValue();
            if(pairChoose.getKey() == SnapType.SNAPS_NUMBER) {
                times.addAll(computeSnapTimes(fxVideo.getVideo(), numChoosed));
            } else {
                long totMilli = fxVideo.getVideo().getLength().toMillis();
                int numSnaps = (int) totMilli / (1000 * numChoosed);
                for(int i = 0; i < numSnaps; i++) {
                    times.add(1000L * numChoosed * (i + 1));
                }
            }

            if(times.isEmpty()) {
                takeSnapshots(stage, fxVideos, pairChoose, finished);
            } else {
                stage.playVideo(fxVideo);
                MediaPlayer mp = stage.getVideoPlayer().getMediaView().getMediaPlayer();
                AtomicBoolean first = new AtomicBoolean(true);
                mp.currentTimeProperty().addListener((obs,o,n) -> {
                    synchronized (times) {
                        if(first.get()) {
                            mp.seek(Duration.millis(times.get(0) - 100));
                            first.set(false);
                        }
                        if(!times.isEmpty()) {
                            boolean doSnap = times.get(0) <= n.toMillis();
                            if(doSnap) {
                                times.remove(0);
                                takeSnapAndAddToModel(stage.getVideoPlayer());
//                                Pair<Path, Long> pair = stage.getVideoPlayer().takeVideoSnapshot(500);
//                                fxVideo.getVideo().getSnapTimes().add(pair.getValue());
//                                model.addSnapshot(fxVideo.getVideo(), pair.getValue(), pair.getKey());
                                if(times.isEmpty()) {
                                    takeSnapshots(stage, fxVideos, pairChoose, finished);
                                } else {
                                    mp.seek(Duration.millis(times.get(0) - 100));
                                }
                            }
                        }
                    }
                });
            }
        }
    }

    public void takeSnapAndAddToModel(JfxVideoPlayer vp) {
        Pair<Path, JkDuration> pair = vp.takeVideoSnapshot(500);
        Video video = vp.getFxVideo().getVideo();
        video.getSnapTimes().add(pair.getValue());
        model.addSnapshot(video, pair.getValue(), pair.getKey());
    }

    private List<Long> computeSnapTimes(Video video, int numSnap) {
        List<Long> times = new ArrayList<>();
        long tot = video.getLength().toMillis();
        long lastSnapFromEnd = 1000 * (tot > 90*1000 ? 8 : 4);
        tot -= lastSnapFromEnd;
        for(int i = 0; i < numSnap; i++) {
            times.add((i+1) * tot / numSnap);
        }
        return times;
    }

    private enum SnapType { SNAPS_NUMBER, SECONDS_BETWEEN_SNAPS }
}
