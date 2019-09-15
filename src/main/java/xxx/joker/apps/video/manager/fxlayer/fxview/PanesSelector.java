package xxx.joker.apps.video.manager.fxlayer.fxview;

import javafx.scene.Scene;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xxx.joker.apps.video.manager.datalayer.entities.Video;
import xxx.joker.apps.video.manager.fxlayer.fxview.panes.HomePane;
import xxx.joker.apps.video.manager.fxlayer.fxview.panes.ManagementPane;
import xxx.joker.apps.video.manager.jfx.controller.CatalogVideoPane;
import xxx.joker.apps.video.manager.jfx.controller.CloseablePane;
import xxx.joker.apps.video.manager.jfx.controller.HomepagePane;
import xxx.joker.apps.video.manager.jfx.controller.MultiVideoPane;

import java.util.List;

public class PanesSelector {

	private static final Logger logger = LoggerFactory.getLogger(PanesSelector.class);

	private static final PanesSelector instance = new PanesSelector();

	private Scene scene;
	private HomePane homePane = new HomePane();
//	private ManagementPane managementPane = new ManagementPane();

	private PanesSelector() {}

	public static PanesSelector getInstance() {
		return instance;
	}

	public void setScene(Scene scene) {
		this.scene = scene;
	}

	public void displayHomePane() {
		homePane.refreshView();
		scene.setRoot(homePane);
		logger.info("Set scene to HOME");
	}

	public void displayManagementPane() {
		scene.setRoot(new ManagementPane());
		logger.info("Set scene to MANAGEMENT");
	}

//	public static void displayMultiVideos() {
//		scene.setRoot(new MultiVideoPane());
//		logger.info("Set scene to MULTI VIDEOS");
//	}

	public boolean isHomeShowed() {
		return scene.getRoot() == homePane;
	}

}