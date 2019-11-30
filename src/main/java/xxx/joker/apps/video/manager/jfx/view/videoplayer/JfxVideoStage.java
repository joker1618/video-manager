package xxx.joker.apps.video.manager.jfx.view.videoplayer;

import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.scenicview.ScenicView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xxx.joker.apps.video.manager.jfx.model.FxVideo;
import xxx.joker.apps.video.manager.NewLauncher;
import xxx.joker.apps.video.manager.jfx.view.videoplayer.JfxVideoPlayer.PlayerConfig;

import java.util.function.Supplier;

public class JfxVideoStage extends Stage {

	private static Logger logger = LoggerFactory.getLogger(JfxVideoStage.class);

	private SimpleObjectProperty<JfxVideoPlayer> videoPlayer = new SimpleObjectProperty<>();
	private PlayerConfig playerConfig;

	protected JfxVideoStage(PlayerConfig config) {
		this.playerConfig = config.cloneConfigs();

		Group root = new Group();
		Scene scene = new Scene(root);
		setScene(scene);

		setOnCloseRequest(e -> close());
	}


	public void setSupplierPrevious(Supplier<FxVideo> supplierPrevious) {
		playerConfig.setPreviousAction(e -> playVideo(supplierPrevious.get()));
	}

	public void setSupplierNext(Supplier<FxVideo> supplierNext) {
		playerConfig.setNextAction(e -> playVideo(supplierNext.get()));
	}

	public PlayerConfig getPlayerConfig() {
		return playerConfig;
	}

	public JfxVideoPlayer getVideoPlayer() {
		return videoPlayer.get();
	}

	public SimpleObjectProperty<JfxVideoPlayer> videoPlayerProperty() {
		return videoPlayer;
	}

	public void playVideo(FxVideo video) {
		if(isShowing()) {
			playerConfig = getVideoPlayer().getPlayerConfig();
			getVideoPlayer().closePlayer();
		}

		JfxVideoPlayer vplayer = new JfxVideoPlayer(video, playerConfig);

		getScene().setRoot(vplayer);
		setTitle(video.getVideo().getTitle());

		if(!isShowing()) {
			show();
			if(NewLauncher.scenicView) {
				ScenicView.show(getScene());
			}
		}

		vplayer.play();
		videoPlayer.set(vplayer);
	}

	@Override
	public void close() {
		if (isShowing()) {
			if(getVideoPlayer() != null) {
				getVideoPlayer().closePlayer();
			}
			super.close();
			logger.trace("closed video stage");
		}
	}
}
