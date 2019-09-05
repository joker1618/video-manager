package xxx.joker.apps.video.manager.jfx.controller.videoplayer;

import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xxx.joker.apps.video.manager.datalayer.entities.Video;
import xxx.joker.apps.video.manager.jfx.controller.videoplayer.JkVideoPlayer.PlayerConfig;

import java.nio.file.Path;
import java.util.function.Supplier;

public class JkVideoStage extends Stage {

	private static Logger logger = LoggerFactory.getLogger(JkVideoStage.class);

	private JkVideoPlayer videoPlayer;
	private PlayerConfig playerConfig;

	protected JkVideoStage(PlayerConfig config, Supplier<FxVideo> previousSupplier, Supplier<FxVideo> nextSupplier) {
		this.playerConfig = config.cloneConfigs();
		if(previousSupplier != null)	playerConfig.setPreviousAction(e -> playVideo(previousSupplier.get()));
		if(nextSupplier != null)		playerConfig.setNextAction(e -> playVideo(nextSupplier.get()));

		Group root = new Group();
		Scene scene = new Scene(root);
		setScene(scene);

		setOnCloseRequest(e -> close());
	}

	public JkVideoPlayer getVideoPlayer() {
		return videoPlayer;
	}

	public void playVideo(FxVideo video) {
		if(isShowing()) {
			playerConfig = videoPlayer.getPlayerConfig();
			videoPlayer.closePlayer();
		}

		videoPlayer = new JkVideoPlayer(video, playerConfig);

		getScene().setRoot(videoPlayer);
		setTitle(video.getVideo().getTitle());

		if(!isShowing()) {
			show();
		}

		videoPlayer.play();
	}

	@Override
	public void close() {
		if (isShowing()) {
			logger.trace("closed video stage");
			videoPlayer.closePlayer();
			super.close();
		}
	}
}
