package xxx.joker.apps.video.manager.jfx.controller.videoplayer;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xxx.joker.apps.video.manager.model.entity.Video;
import xxx.joker.apps.video.manager.jfx.controller.videoplayer.JkVideoPlayer.PlayerConfig;
import xxx.joker.libs.javalibs.javafx.JkFxUtil;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class JkVideoBuilder {

	private static Logger logger = LoggerFactory.getLogger(JkVideoBuilder.class);

	private PlayerConfig playerConfig;
	private Supplier<Video> supplierPrevious;
	private Supplier<Video> supplierNext;
	private JkVideoPlayer lastCreatedPane;

	public JkVideoBuilder() {
		playerConfig = new PlayerConfig();

		// Set defaults
        playerConfig.setVisibleHeading(true);
        playerConfig.setVisiblePlayerBar(true);
		playerConfig.setMiddleMouseClickEvent(
			e -> JkFxUtil.getStage(e).setFullScreen(!JkFxUtil.getStage(e).isFullScreen())
		);
		playerConfig.setRightMouseClickEvent(
			e -> JkFxUtil.getStage(e).setMaximized(!JkFxUtil.getStage(e).isMaximized())
		);
		playerConfig.setCloseEvent(
			e -> JkFxUtil.getStage(e).close()
		);
	}

	public JkVideoBuilder setDecoratedStage(boolean decoratedStage) {
		playerConfig.setDecoratedStage(decoratedStage);
		return this;
	}

	public JkVideoBuilder setShowBorder(boolean showBorder) {
		playerConfig.setShowBorder(showBorder);
		return this;
	}

	public JkVideoBuilder setShowClose(boolean showClose) {
		playerConfig.setShowCloseButton(showClose);
		return this;
	}

	public JkVideoBuilder setHeadingVisible(boolean visible) {
		playerConfig.setVisibleHeading(visible);
		return this;
	}

	public JkVideoBuilder setPlayerBarVisible(boolean visible) {
		playerConfig.setVisiblePlayerBar(visible);
		return this;
	}

	public JkVideoBuilder setCloseEvent(EventHandler<ActionEvent> closeEvent) {
		playerConfig.setCloseEvent(closeEvent);
		return this;
	}

	public JkVideoBuilder setSupplierPrevious(Supplier<Video> supplierPrevious) {
		this.supplierPrevious = supplierPrevious;
		return this;
	}

	public JkVideoBuilder setSupplierNext(Supplier<Video> supplierNext) {
		this.supplierNext = supplierNext;
		return this;
	}

	public JkVideoBuilder setEventMiddleMouseClick(Consumer<MouseEvent> event) {
		playerConfig.setMiddleMouseClickEvent(event);
		return this;
	}

	public JkVideoBuilder setEventRightMouseClick(Consumer<MouseEvent> event) {
		playerConfig.setRightMouseClickEvent(event);
		return this;
	}

	public JkVideoStage createStage() {
		JkVideoStage finalFullStage = new JkVideoStage(playerConfig, supplierPrevious, supplierNext);
		finalFullStage.initStyle(playerConfig.isDecoratedStage() ? StageStyle.DECORATED : StageStyle.UNDECORATED);
		return finalFullStage;
	}

	public JkVideoPlayer createPane(Video video) {
		PlayerConfig config = lastCreatedPane == null ? this.playerConfig : lastCreatedPane.getPlayerConfig();
		JkVideoPlayer pane = new JkVideoPlayer(video, config);
		lastCreatedPane = pane;
		return pane;
	}


}
