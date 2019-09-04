package xxx.joker.apps.video.manager.main;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.scenicview.ScenicView;
import xxx.joker.apps.video.manager.jfx.model.VideoModel;
import xxx.joker.apps.video.manager.jfx.model.VideoModelImpl;

public class CategorizeDirectL extends Application {

	private static boolean scenicView;

	private Stage primaryStage;

	private VideoModel model = VideoModelImpl.getInstance();

	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;

		primaryStage.setTitle("JOKER VIDEO MANAGER");

		// Create scene
		Group root = new Group();
		Scene scene = new Scene(root);
		scene.getStylesheets().add(getClass().getResource("/css/common.css").toExternalForm());

		SceneManager.setScene(scene);
		model.getSelectedVideos().setAll(model.getVideos());
		SceneManager.displayCatalogVideo();

		// Show stage
		primaryStage.setScene(scene);
		primaryStage.setMaximized(true);
		primaryStage.show();

		if(scenicView) {
			ScenicView.show(scene);
		}
	}

	@Override
	public void stop() throws Exception {
		model.	persistData();
	}

	public static void main(String[] args) {
		scenicView = args.length > 0 && args[0].equals("-sv");
		scenicView = true;
		launch(args);
	}

}
