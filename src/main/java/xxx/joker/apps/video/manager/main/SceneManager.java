package xxx.joker.apps.video.manager.main;

import javafx.scene.Scene;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xxx.joker.apps.video.manager.jfx.controller.CatalogVideoPane;
import xxx.joker.apps.video.manager.jfx.controller.CloseablePane;
import xxx.joker.apps.video.manager.jfx.controller.HomepagePane;
import xxx.joker.apps.video.manager.jfx.controller.MultiVideoPane;
import xxx.joker.apps.video.manager.jfx.controller.categories.CategoryManagementPane;

public class SceneManager {

	private static final Logger logger = LoggerFactory.getLogger(SceneManager.class);

	private static Scene mainScene;
	private static HomepagePane homepagePane = new HomepagePane();

	static void setScene(Scene scene) {
		mainScene = scene;
	}

	public static void displayHomepage() {
		if (mainScene.getRoot() instanceof CloseablePane) {
			((CloseablePane) mainScene.getRoot()).closePane();
		}
		homepagePane.refreshView();
		mainScene.setRoot(homepagePane);
		logger.info("Set scene to HOMEPAGE");
	}

	public static void displayCatalogVideo() {
		mainScene.setRoot(new CatalogVideoPane());
		logger.info("Set scene to CATALOG VIDEOS");
	}

	public static void displayMultiVideos() {
		mainScene.setRoot(new MultiVideoPane());
		logger.info("Set scene to MULTI VIDEOS");
	}

	public static void displayCategoryManagement() {
		mainScene.setRoot(new CategoryManagementPane());
		logger.info("Set scene to CATEGORY MANAGEMENT");
	}

	public static boolean isHomepageShowed() {
		return mainScene.getRoot() == homepagePane;
	}

}