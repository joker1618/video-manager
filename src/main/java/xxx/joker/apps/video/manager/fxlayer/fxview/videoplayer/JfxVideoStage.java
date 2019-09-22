package xxx.joker.apps.video.manager.fxlayer.fxview.videoplayer;

import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.scenicview.ScenicView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xxx.joker.apps.video.manager.fxlayer.fxmodel.FxVideo;
import xxx.joker.apps.video.manager.fxlayer.fxview.NewLauncher;
import xxx.joker.apps.video.manager.fxlayer.fxview.videoplayer.JfxVideoPlayer.PlayerConfig;

import java.util.function.Supplier;

public class JfxVideoStage extends Stage {

	private static Logger logger = LoggerFactory.getLogger(JfxVideoStage.class);

	private JfxVideoPlayer videoPlayer;
	private PlayerConfig playerConfig;

//	protected JfxVideoStage(PlayerConfig config) {
//		this(config, null, null);
//	}
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
		return videoPlayer;
	}

	public void playVideo(FxVideo video) {
		if(isShowing()) {
			playerConfig = videoPlayer.getPlayerConfig();
			videoPlayer.closePlayer();
		}

		videoPlayer = new JfxVideoPlayer(video, playerConfig);

		getScene().setRoot(videoPlayer);
		setTitle(video.getVideo().getTitle());

		if(!isShowing()) {
			show();
			if(NewLauncher.scenicView) {
				ScenicView.show(getScene());
			}
		}

		videoPlayer.play();
	}

	@Override
	public void close() {
		if (isShowing()) {
			videoPlayer.closePlayer();
			logger.trace("closed video stage");
			super.close();
		}
	}
}
