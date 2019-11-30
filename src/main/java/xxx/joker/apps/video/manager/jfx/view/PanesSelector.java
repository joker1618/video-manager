package xxx.joker.apps.video.manager.jfx.view;

import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xxx.joker.apps.video.manager.datalayer.entities.Video;
import xxx.joker.apps.video.manager.jfx.model.FxModel;
import xxx.joker.apps.video.manager.jfx.model.FxVideo;
import xxx.joker.apps.video.manager.jfx.view.panes.*;
import xxx.joker.apps.video.manager.provider.VideoStagesPosition;

import java.util.List;

public class PanesSelector {

	private static final Logger LOG = LoggerFactory.getLogger(PanesSelector.class);

	private static final PanesSelector instance = new PanesSelector();

	private FxModel model = FxModel.getModel();

	private Scene scene;
	private HomePane homePane;
	private SimpleObjectProperty<Closeable> actualPane;

	private PanesSelector() {
		this.homePane = new HomePane();
		this.actualPane = new SimpleObjectProperty<>();
		actualPane.addListener((obs,o,n) -> {
			if(n != o && o != null) {
				o.closePane();
			}
		});
	}

	public static PanesSelector getInstance() {
		return instance;
	}

	public void setScene(Scene scene) {
		this.scene = scene;
	}

	public void displayHomePane() {
		if(actualPane.get() != homePane) {
			actualPane.set(homePane);
			homePane.refreshView();
			scene.setRoot(homePane);
			LOG.debug("Set scene to HOME");
		}
	}

	public HomePane getHomePane() {
		return homePane;
	}

	public void displayManagementPane() {
		ManagementPane mngPane = new ManagementPane(model.getSelectedVideos());
		scene.setRoot(mngPane);
		actualPane.set(mngPane);
		LOG.debug("Set scene to MANAGEMENT");
	}

	public void displayCutVideoPane(Video video) {
		CutVideoPane pane = new CutVideoPane(video);
		scene.setRoot(pane);
		actualPane.set(pane);
		LOG.debug("Set scene to CUT VIDEO");
	}

	public void displayMultiVideoPane(VideoStagesPosition stagesPosition, List<FxVideo> fxVideos) {
		MultiDisplayPane pane = new MultiDisplayPane(stagesPosition, fxVideos);
		scene.setRoot(pane);
		actualPane.set(pane);
		LOG.debug("Set scene to DISPLAY MULTI VIDEOS");
	}

	public boolean isHomeShowed() {
		return scene.getRoot() == homePane;
	}

}