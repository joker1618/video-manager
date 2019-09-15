package xxx.joker.apps.video.manager.fxlayer.fxview.videoplayer;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.stage.StageStyle;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xxx.joker.apps.video.manager.fxlayer.fxmodel.FxVideo;
import xxx.joker.apps.video.manager.fxlayer.fxview.videoplayer.JfxVideoPlayer.PlayerConfig;
import xxx.joker.libs.core.javafx.JfxUtil;

import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class JfxVideoBuilder {

	private static Logger logger = LoggerFactory.getLogger(JfxVideoBuilder.class);

	private PlayerConfig playerConfig;
	private Supplier<FxVideo> supplierPrevious;
	private Supplier<FxVideo> supplierNext;
	private JfxVideoPlayer lastCreatedPane;

	public JfxVideoBuilder() {
		playerConfig = new PlayerConfig();

		// Set defaults
		playerConfig.setVisibleHeading(true);
		playerConfig.setVisiblePlayerBar(true);
		playerConfig.setShowCloseButton(true);
		playerConfig.setLeftMouseType(PlayerConfig.LeftMouseType.PLAY);
		playerConfig.setCloseEvent(
				e -> JfxUtil.getStage(e).close()
		);
		playerConfig.setMiddleMouseClickEvent(
				e -> JfxUtil.getStage(e).setFullScreen(!JfxUtil.getStage(e).isFullScreen())
		);
		playerConfig.setRightMouseClickEvent(
				e -> JfxUtil.getStage(e).setMaximized(!JfxUtil.getStage(e).isMaximized())
		);
	}

	public JfxVideoBuilder setDecoratedStage(boolean decoratedStage) {
		playerConfig.setDecoratedStage(decoratedStage);
		return this;
	}

	public JfxVideoBuilder setShowBorder(boolean showBorder) {
		playerConfig.setShowBorder(showBorder);
		return this;
	}

	public JfxVideoBuilder setShowClose(boolean showClose) {
		playerConfig.setShowCloseButton(showClose);
		return this;
	}

	public JfxVideoBuilder setHeadingVisible(boolean visible) {
		playerConfig.setVisibleHeading(visible);
		return this;
	}

	public JfxVideoBuilder setPlayerBarVisible(boolean visible) {
		playerConfig.setVisiblePlayerBar(visible);
		return this;
	}

	public JfxVideoBuilder setCloseEvent(EventHandler<ActionEvent> closeEvent) {
		playerConfig.setCloseEvent(closeEvent);
		return this;
	}

	public JfxVideoBuilder setSupplierPrevious(Supplier<FxVideo> supplierPrevious) {
		this.supplierPrevious = supplierPrevious;
		return this;
	}

	public JfxVideoBuilder setSupplierNext(Supplier<FxVideo> supplierNext) {
		this.supplierNext = supplierNext;
		return this;
	}

	public JfxVideoBuilder setEventMiddleMouseClick(Consumer<MouseEvent> event) {
		playerConfig.setMiddleMouseClickEvent(event);
		return this;
	}

	public JfxVideoBuilder setEventRightMouseClick(Consumer<MouseEvent> event) {
		playerConfig.setRightMouseClickEvent(event);
		return this;
	}

	public JfxVideoBuilder setBtnCameraRunnable(Runnable btnCameraListener) {
		playerConfig.setBtnCameraRunnable(btnCameraListener);
		return this;
	}

	public JfxVideoStage createStage() {
		JfxVideoStage finalFullStage = new JfxVideoStage(playerConfig, supplierPrevious, supplierNext);
		finalFullStage.initStyle(playerConfig.isDecoratedStage() ? StageStyle.DECORATED : StageStyle.UNDECORATED);
		return finalFullStage;
	}

	public JfxVideoPlayer createPane(FxVideo video) {
		PlayerConfig config = lastCreatedPane == null ? this.playerConfig : lastCreatedPane.getPlayerConfig();
		JfxVideoPlayer pane = new JfxVideoPlayer(video, config);
		lastCreatedPane = pane;
		return pane;
	}

}
