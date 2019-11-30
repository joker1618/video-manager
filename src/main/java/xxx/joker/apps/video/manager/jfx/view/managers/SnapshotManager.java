package xxx.joker.apps.video.manager.jfx.view.managers;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xxx.joker.apps.video.manager.datalayer.entities.Video;
import xxx.joker.apps.video.manager.jfx.model.FxModel;
import xxx.joker.apps.video.manager.jfx.model.FxVideo;
import xxx.joker.apps.video.manager.jfx.view.gridpane.GridPaneBuilder;
import xxx.joker.apps.video.manager.jfx.view.videoplayer.JfxVideoBuilder;
import xxx.joker.apps.video.manager.jfx.view.videoplayer.JfxVideoPlayer;
import xxx.joker.apps.video.manager.jfx.view.videoplayer.JfxVideoStage;
import xxx.joker.libs.core.datetime.JkDuration;
import xxx.joker.libs.core.lambda.JkStreams;
import xxx.joker.libs.core.util.JkConvert;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static xxx.joker.libs.core.util.JkStrings.strf;

public class SnapshotManager {

    private static final Logger LOG = LoggerFactory.getLogger(SnapshotManager.class);

    private FxModel model = FxModel.getModel();

    public SnapshotManager() {
    }

    public SimpleBooleanProperty runAutoSnap(Collection<Video> videos) {
        List<FxVideo> fxVideos = model.toFxVideos(videos);
        Pair<SnapType, Integer[]> pair = askAutoSnapType();
        if(pair != null) {
            int count = JkStreams.count(Arrays.asList(pair.getValue()), n -> n == null || n <= 0);
            if(count > 0) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("Invalid numbers");
                alert.showAndWait();
            } else {
                JfxVideoBuilder videoBuilder = new JfxVideoBuilder();
                videoBuilder.setShowBorder(true);
                JfxVideoStage stage = videoBuilder.createStage();
                stage.setMaximized(true);
                SimpleBooleanProperty finished = new SimpleBooleanProperty(false);
                takeSnapshots(stage, fxVideos, pair, finished, 0);
                return finished;
            }
        }
        return null;
    }

    private Pair<SnapType, Integer[]> askAutoSnapType() {
        Dialog<ButtonType> dlg = new Dialog<>();
        GridPaneBuilder gpBuilder = new GridPaneBuilder();
        RadioButton rbNumSnap = new RadioButton("Num snaps:");
        TextField txtNumSnap = new TextField("15");
        RadioButton rbSmart = new RadioButton("Smart");
        TextField txtMultipleOf = new TextField("3");
        TextField txtMaxNumRows = new TextField("8");
        txtNumSnap.disableProperty().bind(rbSmart.selectedProperty());
        txtMultipleOf.disableProperty().bind(rbNumSnap.selectedProperty());
        txtMaxNumRows.disableProperty().bind(rbNumSnap.selectedProperty());
        ToggleGroup tg = new ToggleGroup();
        tg.getToggles().addAll(rbNumSnap, rbSmart);
        rbSmart.setSelected(true);
        gpBuilder.add(0, 0, rbSmart);
        gpBuilder.add(0, 1, new Label("Multiplier:"));
        gpBuilder.add(0, 2, txtMultipleOf);
        gpBuilder.add(1, 1, new Label("Max num rows:"));
        gpBuilder.add(1, 2, txtMaxNumRows);
        gpBuilder.add(2, 0, rbNumSnap);
        gpBuilder.add(2, 1, txtNumSnap);
        DialogPane dialogPane = new DialogPane();
        dialogPane.setContent(gpBuilder.createGridPane());
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dlg.setDialogPane(dialogPane);
        Optional<ButtonType> optional = dlg.showAndWait();
        if(optional.isPresent() && optional.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
            SnapType snapType = rbNumSnap.isSelected() ? SnapType.SNAPS_NUMBER : SnapType.SMART;
            Integer[] nums;
            if(snapType == SnapType.SNAPS_NUMBER) {
                Integer num = JkConvert.toInt(txtNumSnap.getText());
                nums = new Integer[]{num};
            } else if(snapType == SnapType.SMART) {
                Integer mult = JkConvert.toInt(txtMultipleOf.getText());
                Integer maxRows = JkConvert.toInt(txtMaxNumRows.getText());
                nums = new Integer[]{mult, maxRows * mult};
            } else {
                nums = new Integer[0];
            }
            return Pair.of(snapType, nums);
        }
        return null;
    }

    private void takeSnapshots(JfxVideoStage stage, List<FxVideo> fxVideos, Pair<SnapType, Integer[]> pairChoose, SimpleBooleanProperty finished, int index) {
        if(index >= fxVideos.size()) {
            stage.close();
            finished.setValue(true);
        } else {
            FxVideo fxVideo = fxVideos.get(index);

            List<Long> times = new ArrayList<>();
            if(pairChoose.getKey() == SnapType.SNAPS_NUMBER) {
                times.addAll(computeSnapTimesByFixedNumber(fxVideo.getVideo(), pairChoose.getValue()[0]));
            } else {
                times.addAll(computeSnapTimesSmart(fxVideo.getVideo(), pairChoose.getValue()));
            }

            if(times.isEmpty()) {
                takeSnapshots(stage, fxVideos, pairChoose, finished, index + 1);
            } else {
                stage.playVideo(fxVideo);
                Label lblCounter = new Label(strf("{}/{}", index + 1, fxVideos.size()));
                ((HBox)stage.getVideoPlayer().getTop()).getChildren().add(0, lblCounter);
                MediaPlayer mp = stage.getVideoPlayer().getMediaView().getMediaPlayer();
                double rate = 0.2;
                mp.setRate(rate);
                AtomicBoolean first = new AtomicBoolean(true);
                mp.currentTimeProperty().addListener((obs,o,n) -> {
                    synchronized (times) {
                        if(first.get()) {
                            mp.seek(Duration.millis(times.get(0) - 100 * rate));
                            first.set(false);
                        }
                        if(!times.isEmpty()) {
                            boolean doSnap = times.get(0) <= n.toMillis();
                            if(doSnap) {
                                times.remove(0);
                                takeSnapAndAddToModel(stage.getVideoPlayer());
                                if(times.isEmpty()) {
                                    takeSnapshots(stage, fxVideos, pairChoose, finished, index + 1);
                                } else {
                                    mp.seek(Duration.millis(times.get(0) - 100 * rate));
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

    private List<Long> computeSnapTimesByFixedNumber(Video video, int numSnap) {
        List<Long> times = new ArrayList<>();
        long tot = video.getLength().toMillis();
        long lastSnapFromEnd = 1000 * (tot > 90*1000 ? 8 : 4);
        tot -= lastSnapFromEnd;
        for(int i = 0; i < numSnap; i++) {
            times.add((i+1) * tot / numSnap);
        }
        return times;
    }

    private List<Long> computeSnapTimesSmart(Video video, Integer[] nums) {
        List<Long> times = new ArrayList<>();
        long tot = video.getLength().toMillis();
        long lastSnapFromEnd = 1000 * (tot > 90*1000 ? 8 : 4);
        tot -= lastSnapFromEnd;
        int mult = nums[0];
        int maxNum = nums[1];
        int numRowsRound = (int)(tot / (3 * 60 * 1000));
        int numSnaps = 2 * mult;
        if(numRowsRound > 1) {
            numSnaps += mult * (numRowsRound - 1);
        }
        numSnaps = Math.min(maxNum, numSnaps);

        for(int i = 0; i < numSnaps; i++) {
            times.add((i+1) * tot / numSnaps);
        }
        return times;
    }

    private enum SnapType { SNAPS_NUMBER, SMART}
}
