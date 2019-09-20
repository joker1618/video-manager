package xxx.joker.apps.video.manager.fxlayer.fxview;

import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xxx.joker.apps.video.manager.datalayer.entities.Video;
import xxx.joker.apps.video.manager.fxlayer.fxmodel.FxModel;
import xxx.joker.apps.video.manager.fxlayer.fxview.panes.Closeable;
import xxx.joker.apps.video.manager.fxlayer.fxview.panes.CutVideoPane;
import xxx.joker.apps.video.manager.fxlayer.fxview.panes.HomePane;
import xxx.joker.apps.video.manager.fxlayer.fxview.panes.ManagementPane;

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
		homePane.refreshView();
		scene.setRoot(homePane);
		actualPane.set(homePane);
		LOG.debug("Set scene to HOME");
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

	public boolean isHomeShowed() {
		return scene.getRoot() == homePane;
	}

}