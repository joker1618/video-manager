package xxx.joker.apps.video.manager.jfx.fxview.panes;

import javafx.beans.InvalidationListener;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xxx.joker.apps.video.manager.jfx.fxmodel.FxVideo;
import xxx.joker.apps.video.manager.jfx.fxview.PanesSelector;
import xxx.joker.apps.video.manager.jfx.fxview.managers.Playlist;
import xxx.joker.apps.video.manager.jfx.fxview.videoplayer.JfxVideoBuilder;
import xxx.joker.apps.video.manager.jfx.fxview.videoplayer.JfxVideoPlayer;
import xxx.joker.apps.video.manager.jfx.fxview.videoplayer.JfxVideoStage;
import xxx.joker.apps.video.manager.provider.VideoStagesPosition;
import xxx.joker.libs.core.datetime.JkDuration;
import xxx.joker.libs.core.datetime.JkTimer;
import xxx.joker.libs.core.lambdas.JkStreams;
import xxx.joker.libs.core.tests.JkTests;

import java.util.List;

import static xxx.joker.libs.core.javafx.JfxControls.createVBox;

public class MultiDisplayPane extends BorderPane implements Closeable {

    private static final Logger LOG = LoggerFactory.getLogger(MultiDisplayPane.class);

    private List<JfxVideoStage> stages;


    public MultiDisplayPane(VideoStagesPosition stagesPosition, List<FxVideo> fxVideos) {
        getStylesheets().add(getClass().getResource("/css/MultiDisplayPane.css").toExternalForm());

        setCenter(createMainPane());

        Playlist playlist = new Playlist(fxVideos);
        displayMultiVideos(stagesPosition, playlist);
    }

    private Pane createMainPane() {
        Button btnToFront = new Button("TO FRONT");
        btnToFront.setOnAction(e -> stages.forEach(Stage::toFront));

        Button btnToBack = new Button("TO BACK");
        btnToBack.setOnAction(e -> stages.forEach(Stage::toBack));

        Button btnExit = new Button("EXIT");
        btnExit.setOnAction(e -> PanesSelector.getInstance().displayHomePane());

        return createVBox("container", btnToFront, btnToBack, btnExit);
    }

    private void displayMultiVideos(VideoStagesPosition stagesPosition, Playlist playlist) {
        JfxVideoBuilder pbuilder = new JfxVideoBuilder();
        pbuilder.setDecoratedStage(false);
        pbuilder.setShowBorder(true);
        pbuilder.setShowClose(true);
        pbuilder.setHeadingVisible(false);
        pbuilder.setPlayerBarVisible(false);
        pbuilder.setVisibleBtnCamera(true);
        pbuilder.setVisibleBtnMark(true);
        pbuilder.setLeftMouseType(JfxVideoPlayer.PlayerConfig.LeftMouseType.SHOW_HIDE);
        pbuilder.setCloseRunnable(PanesSelector.getInstance()::displayHomePane);

        this.stages = pbuilder.createStages(stagesPosition.getNumStages());
        String nameDisplay = stagesPosition.getName();
        for(int i = 0; i < stages.size(); i++)  {
            JfxVideoStage stage = stages.get(i);
            int stageNum = i;
            stage.setSupplierPrevious(() -> playlist.previousVideo(stageNum));
            stage.setSupplierNext(() -> playlist.nextVideo(stageNum));
            if(StringUtils.equalsAny(nameDisplay, "BIG_CENTRAL_2", "BIG_CENTRAL_3")) {
                int lengthMilli = (nameDisplay.equals("BIG_CENTRAL_2") ? 2 : 2) * 60 * 1000;
                if(nameDisplay.equals("BIG_CENTRAL_2")) {
                    playlist.removeVideos(v -> v.getVideo().getLength().toMillis() < lengthMilli);
                }
                stage.videoPlayerProperty().addListener(obs -> {
                    JfxVideoPlayer vp = stage.getVideoPlayer();
                    if (vp != null) {
                        long start = vp.getFxVideo().getVideo().getLength().toMillis() - lengthMilli;
                        MediaPlayer mp = vp.getMediaView().getMediaPlayer();
                        SimpleBooleanProperty seekDone = new SimpleBooleanProperty(false);
                        ChangeListener<Duration> event = (ob, o, n) -> {
//                            LOG.debug("Seek for {}", vp.getFxVideo().getVideo().getTitle());
                            mp.seek(Duration.millis(start));
                            seekDone.set(true);
                        };
                        mp.currentTimeProperty().addListener(event);
                        seekDone.addListener(done -> mp.currentTimeProperty().removeListener(event));
                    }
                });
            }
            stage.playVideo(playlist.nextVideo(stageNum));
            stage.getPlayerConfig().setBottomPropertyListener(() -> {
                JkStreams.filter(stages, s -> s != stage).forEach(s -> {
                    JfxVideoPlayer vp = stage.getVideoPlayer();
                    s.getVideoPlayer().setShowTopBottom(vp.getTop() != null, vp.getBottom() != null);
                });
            });
        }
        stagesPosition.setStagesPosition(stages);
    }

    @Override
    public void closePane() {
        if(!stages.isEmpty()) {
            LOG.debug("Closing {} video stages", stages.size());
            stages.forEach(JfxVideoStage::close);
            stages.clear();
        }
    }
}
