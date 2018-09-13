package xxx.joker.apps.video.manager.jfx.controller.videoplayer;

import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xxx.joker.apps.video.manager.data.beans.Video;
import xxx.joker.apps.video.manager.jfx.controller.videoplayer.JkVideoPlayer.PlayerConfig;

import java.util.function.Supplier;

public class JkVideoStage extends Stage {

	private static Logger logger = LoggerFactory.getLogger(JkVideoStage.class);

	private JkVideoPlayer videoPlayer;
	private PlayerConfig playerConfig;

	protected JkVideoStage(PlayerConfig config, Supplier<Video> previousSupplier, Supplier<Video> nextSupplier) {
		this.playerConfig = config.cloneConfigs();
		if(previousSupplier != null)	playerConfig.setPreviousAction(e -> playVideo(previousSupplier.get()));
		if(nextSupplier != null)		playerConfig.setNextAction(e -> playVideo(nextSupplier.get()));

		Group root = new Group();
		Scene scene = new Scene(root);
		setScene(scene);

		setOnCloseRequest(e -> close());
	}

	public void playVideo(Video video) {
		if(isShowing()) {
			playerConfig = videoPlayer.getPlayerConfig();
			videoPlayer.closePlayer();
		}

		videoPlayer = new JkVideoPlayer(video, playerConfig);

		getScene().setRoot(videoPlayer);
		setTitle(video.getPath().toString());

		if(!isShowing()) {
			show();
		}

		videoPlayer.play();
	}

	@Override
	public void close() {
		if (isShowing()) {
			logger.trace("close video stage");
			logger.trace("VideoStage dim: {}x{}  {}x{}", getWidth(), getHeight(), getScene().getWidth(), getScene().getHeight());
			videoPlayer.closePlayer();
			super.close();
		}
	}
}
