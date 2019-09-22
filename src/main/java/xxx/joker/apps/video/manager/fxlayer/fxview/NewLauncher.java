package xxx.joker.apps.video.manager.fxlayer.fxview;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.scenicview.ScenicView;
import xxx.joker.apps.video.manager.common.Config;
import xxx.joker.apps.video.manager.fxlayer.fxmodel.FxModel;
import xxx.joker.libs.core.files.JkFiles;

public class NewLauncher extends Application {

    public static boolean scenicView;

    private PanesSelector panesSel = PanesSelector.getInstance();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("JOKER VIDEO MANAGER");

        // Create scene
        Group root = new Group();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/common.css").toExternalForm());

        panesSel.setScene(scene);
        panesSel.displayHomePane();

        // Show stage
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();

        if (scenicView) {
            ScenicView.show(scene);
        }

        Platform.setImplicitExit(false);
        primaryStage.setOnCloseRequest(e -> {
            if (panesSel.isHomeShowed()) {
                Platform.exit();
            } else {
                e.consume();
                panesSel.displayHomePane();
            }
        });
    }

    @Override
    public void stop() throws Exception {
        panesSel.getHomePane().closePane();
        JkFiles.delete(Config.TEMP_FOLDER);
        FxModel.getModel().persistData();
    }

    public static void main(String[] args) {
        scenicView = args.length == 1 && "-sv".equals(args[0]);
//		scenicView = true;
        launch(args);
    }

}