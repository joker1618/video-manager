package xxx.joker.apps.video.manager.jfx.fxview.panes;

import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xxx.joker.apps.video.manager.jfx.fxmodel.FxVideo;
import xxx.joker.apps.video.manager.jfx.fxview.PanesSelector;
import xxx.joker.apps.video.manager.jfx.fxview.managers.Playlist;
import xxx.joker.apps.video.manager.jfx.fxview.videoplayer.JfxVideoBuilder;
import xxx.joker.apps.video.manager.jfx.fxview.videoplayer.JfxVideoPlayer;
import xxx.joker.apps.video.manager.jfx.fxview.videoplayer.JfxVideoStage;
import xxx.joker.apps.video.manager.provider.VideoStagesPosition;
import xxx.joker.libs.core.lambdas.JkStreams;

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
        for(int i = 0; i < stages.size(); i++)  {
            JfxVideoStage stage = stages.get(i);
            int stageNum = i;
            stage.setSupplierPrevious(() -> playlist.previousVideo(stageNum));
            stage.setSupplierNext(() -> playlist.nextVideo(stageNum));
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
