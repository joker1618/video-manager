package xxx.joker.apps.video.manager.main;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.scenicview.ScenicView;
import xxx.joker.apps.video.manager.jfx.model.VideoModelImpl;

public class OnlyLauncherL extends Application {

	private static boolean scenicView;

	private Stage primaryStage;

	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;

		primaryStage.setTitle("JOKER VIDEO MANAGER");

		// Create scene
		Group root = new Group();
		Scene scene = new Scene(root);

		SceneManager.setScene(scene);
		SceneManager.displayHomepage();

		// Show stage
		primaryStage.setScene(scene);
		primaryStage.setMaximized(true);
//		primaryStage.sizeToScene();
		primaryStage.show();

		if(scenicView) {
			ScenicView.show(scene);
		}

		Platform.setImplicitExit(false);
		primaryStage.setOnCloseRequest(e -> {
			if(SceneManager.isHomepageShowed()) {
				Platform.exit();
			} else {
				e.consume();
				SceneManager.displayHomepage();
			}
		});
	}

	@Override
	public void stop() throws Exception {
		VideoModelImpl.getInstance().persistData();
	}

	public static void main(String[] args) {
		scenicView = args.length > 0 && StringUtils.equalsAnyIgnoreCase(args[0], "-sv", "-scenicView");
//		scenicView = true;
		launch(args);
	}

}
